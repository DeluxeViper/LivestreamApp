package com.deluxe_viper.livestreamapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

typealias LumaListener = (luma: Double, image: ByteArray) -> Unit


class MainActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var isStreaming: Boolean = false
    private var isRecording: Boolean = false
    private lateinit var previewBuffer: ByteArray
    private lateinit var udpSocket: DatagramSocket
    private lateinit var address: InetAddress
    private var port: Int? = null
    private var encDataList: ArrayList<ByteArray> = ArrayList<ByteArray>()
    private var encDataLengthList: ArrayList<Int> = ArrayList<Int>()

    private lateinit var bitmapBuffer: Bitmap

    var senderRun = Runnable {
        while (isStreaming) {
            var empty = false;
            var encData: ByteArray? = null

            synchronized(encDataList) {
                if (encDataList.size == 0)
                    empty = true
                else
                    encData = encDataList.removeAt(0)
            }

            if (empty) {
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                continue
            }
            try {
                val packet =
                    DatagramPacket(encData, encData!!.size, address, port!!)
                udpSocket.send(packet)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        camera_capture_button.setOnClickListener { takePhoto() }
        camera_record_button.setOnClickListener {
            if (!isRecording) {
                startRecording()
                camera_record_button.setText("Stop Recording")
            } else {
                stopRecording()
                camera_record_button.setText("Start Recording")
            }
        }

        camera_stop_record_button.setOnClickListener { stopRecording() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @ExperimentalStdlibApi
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            "${SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())}.jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    @SuppressLint("RestrictedApi")
    private fun startRecording() {
        val videoFile = File(
            outputDirectory,
            "${SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())}.mp4"
        )
        val outputOptions = VideoCapture.OutputFileOptions.Builder(videoFile).build()

        videoCapture?.startRecording(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : VideoCapture.OnVideoSavedCallback {
                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    Log.e(TAG, "Video capture failed: $message")
                }

                override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(videoFile)
                    val msg = "Video capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun startStream(ip: String, port: Int, width: Int, height: Int) {
        val sp: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)

        try {
            this.udpSocket = DatagramSocket()
            this.address = InetAddress.getByName(ip)
            this.port = port
        } catch (e: SocketException) {
            e.printStackTrace()
            return
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return;
        }

        sp.edit().putString(SP_DEST_IP, ip).apply()
        sp.edit().putInt(SP_DEST_PORT, port).apply()

        this.isStreaming = true
        val thread = Thread(senderRun)
        thread.start()
    }

    private fun stopStream() {
        this.isStreaming = false
    }

    @SuppressLint("RestrictedApi")
    private fun stopRecording() {
        videoCapture?.stopRecording()
        Log.d(TAG, "stopRecording")
    }

    @SuppressLint("RestrictedApi")
    @ExperimentalStdlibApi
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build().also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }


            // Set up the image analysis use case which will process frames in real time
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            var frameCounter = 0
            var lastFpsTimestamp = System.currentTimeMillis()
            val converter = YuvToRgbConverter(this)

            imageCapture = ImageCapture.Builder()
                .build()

//            videoCapture = VideoCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                if (!::bitmapBuffer.isInitialized) {
                    // The image rotation and RGB image buffer are initialized only once
                    // the analyzer has started running
                    bitmapBuffer =
                        Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                }

                // Convert the image to RGB and place it in our shared buffer
                converter.yuvToRgb(image.image!!, bitmapBuffer)

                // Convert bitmapBuffer into base64
                val outputStream = ByteArrayOutputStream()
                bitmapBuffer.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                var byteArray = outputStream.toByteArray()
                val encoded: String = Base64.getEncoder().encodeToString(byteArray) // Base64 string

//                if (this.isStreaming) {
//                    if (this.encDataLengthList.size > 100) {
//                        Log.e(TAG, "OUT OF BUFFER");
//                        return@Analyzer
//                    }
//
//                    val encData : ByteArray? = this.encoder!!.offerEncoder(byteArray);
//                    if (encData!!.isNotEmpty()) {
//                        synchronized(this.encDataList) {
//                            this.encDataList.add(encData)
//                        }
//                    }
//                }

                Log.d(TAG, "Encoded:")
//                longLog(encoded)
                // Compute the FPS of the entire pipeline
                val frameCount = 10
                if (++frameCounter % frameCount == 0) {
                    frameCounter = 0
                    val now = System.currentTimeMillis()
                    val delta = now - lastFpsTimestamp
                    val fps = 1000 * frameCount.toFloat() / delta
                    Log.d(TAG, "%.02f".format(fps))
                    lastFpsTimestamp = now
                }

                image.close()
            })
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun longLog(str: String) {
        if (str.length > 4000) {
            Log.d("", str.substring(0, 4000))
            longLog(str.substring(4000))
        } else Log.d("", str)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        private const val SP_CAM_WIDTH = "cam_width";
        private const val SP_CAM_HEIGHT = "cam_height";
        private const val SP_DEST_IP = "dest_ip";
        private const val SP_DEST_PORT = "dest_port";

        private const val DEFAULT_FRAME_RATE = 15;
        private const val DEFAULT_BIT_RATE = 500000;
    }
}