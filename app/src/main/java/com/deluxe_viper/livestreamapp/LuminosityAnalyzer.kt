package com.deluxe_viper.livestreamapp

import android.graphics.*
import android.util.Base64.encodeToString
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*

class LuminosityAnalyzer(private val lumaListener: LumaListener) : ImageAnalysis.Analyzer {
    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(capacity())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    private fun imageProxyToByteArray(image: ImageProxy): ByteArray {
        val yuvBytes = ByteArray(image.width * (image.height + image.height / 2))
        val yPlane = image.planes[0].buffer
        val uPlane = image.planes[1].buffer
        val vPlane = image.planes[2].buffer

        yPlane.get(yuvBytes, 0, image.width * image.height)

        val chromaRowStride = image.planes[1].rowStride
        val chromaRowPadding = chromaRowStride - image.width / 2

        var offset = image.width * image.height
        if (chromaRowPadding == 0) {

            uPlane.get(yuvBytes, offset, image.width * image.height / 4)
            offset += image.width * image.height / 4
            vPlane.get(yuvBytes, offset, image.width * image.height / 4)
        } else {
            for (i in 0 until image.height / 2) {
                uPlane.get(yuvBytes, offset, image.width / 2)
                offset += image.width / 2
                if (i < image.height / 2 - 2) {
                    uPlane.position(uPlane.position() + chromaRowPadding)
                }
            }
            for (i in 0 until image.height / 2) {
                vPlane.get(yuvBytes, offset, image.width / 2)
                offset += image.width / 2
                if (i < image.height / 2 - 1) {
                    vPlane.position(vPlane.position() + chromaRowPadding)
                }
            }
        }

        return yuvBytes
    }

    fun ImageProxy.toBitmap(): ByteArray {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        return imageBytes
    }

    fun Bitmap.toByteArray(): ByteArray {
        ByteArrayOutputStream().apply {
            compress(Bitmap.CompressFormat.JPEG, 10, this)
            return toByteArray()
        }
    }

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        Log.d("LuminosityAnalyzer", "analyze: image proxy format ${image.format}")
        val data = buffer.toByteArray()

        val bitmap = image.toBitmap()
        var bitmapBuffer = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)


//        val bitmapBA = bitmap.toByteArray()
//        val imageBytes = image.toByteArray()
        Log.d("LuminosityAnalyzer", "analyze: imagebytes ${bitmap.joinToString()}")

//        val base64 = Base64.getDecoder().decode(data)
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

//        val imageByteArray = imageProxyToByteArray(image)

//        imageListener(imageByteArray)


        lumaListener(luma, bitmap)
        image.close()
    }
}