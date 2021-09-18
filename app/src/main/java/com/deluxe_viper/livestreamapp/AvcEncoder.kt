package com.deluxe_viper.livestreamapp

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecList
import android.media.MediaFormat
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class AvcEncoder {

    var mediaCodec: MediaCodec? = null
    var width = 0
    var height = 0
    var timeoutUSec = 10000
    var frameIndex: Long = 0
    var spsPpsInfo: ByteArray? = null
    var yuv420: ByteArray? = null
    var frameRate = 0
    var yStride = 0
    var cStride = 0
    var ySize = 0
    var cSize = 0
    var halfWidth = 0
    var halfHeight = 0

    var outputStream = ByteArrayOutputStream()

    fun init(width: Int, height: Int, framerate: Int, bitrate: Int): Boolean {
        mediaCodec = try {
            MediaCodec.createEncoderByType(MIME_TYPE)
        } catch (e: IOException) {
            return false
        }
        var isSupport = false
        val colorFormat = 0
        val codecInfo =
            selectCodec(MIME_TYPE)
        val capabilities =
            codecInfo!!.getCapabilitiesForType(MIME_TYPE)
        var i = 0
        while (i < capabilities.colorFormats.size && colorFormat == 0) {
            val format = capabilities.colorFormats[i]
            if (format == CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                isSupport = true
                break
            }
            i++
        }
        if (!isSupport) return false
        this.width = width
        this.height = height
        halfWidth = width / 2
        halfHeight = height / 2
        frameRate = framerate
        yStride = Math.ceil(width / 16.0f.toDouble()).toInt() * 16
        cStride = Math.ceil(width / 32.0f.toDouble()).toInt() * 16
        ySize = yStride * height
        cSize = cStride * height / 2
        yuv420 = ByteArray(width * height * 3 / 2)
        val mediaFormat =
            MediaFormat.createVideoFormat(MIME_TYPE, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate)
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        )
        mediaFormat.setInteger(
            MediaFormat.KEY_I_FRAME_INTERVAL,
            I_FRAME_INTERVAL
        )
        mediaCodec!!.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec!!.start()
        return true
    }

    fun close() {
        try {
            mediaCodec!!.stop()
            mediaCodec!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun offerEncoder(input: ByteArray): ByteArray? {
        YV12toYUV420PackedSemiPlanar(input, yuv420, width, height)
        try {
            val inputBuffers = mediaCodec!!.inputBuffers
            val outputBuffers = mediaCodec!!.outputBuffers
            val inputBufferIndex = mediaCodec!!.dequeueInputBuffer(-1)
            if (inputBufferIndex >= 0) {
                val pts = computePresentationTime(frameIndex, frameRate)
                val inputBuffer = inputBuffers[inputBufferIndex]
                inputBuffer.clear()
                inputBuffer.put(yuv420, 0, yuv420!!.size)
                mediaCodec!!.queueInputBuffer(inputBufferIndex, 0, yuv420!!.size, pts, 0)
                frameIndex++
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex =
                mediaCodec!!.dequeueOutputBuffer(bufferInfo, timeoutUSec.toLong())
            while (outputBufferIndex >= 0) {
                val outputBuffer = outputBuffers[outputBufferIndex]
                val outData = ByteArray(bufferInfo.size)
                outputBuffer[outData]
                if (spsPpsInfo == null) {
                    val spsPpsBuffer = ByteBuffer.wrap(outData)
                    if (spsPpsBuffer.int == 0x00000001) {
                        spsPpsInfo = ByteArray(outData.size)
                        System.arraycopy(outData, 0, spsPpsInfo, 0, outData.size)
                    } else {
                        return null
                    }
                } else {
                    outputStream.write(outData)
                }
                mediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex =
                    mediaCodec!!.dequeueOutputBuffer(bufferInfo, timeoutUSec.toLong())
            }
            val ret = outputStream.toByteArray()
            if (ret.size > 5 && ret[4].toInt() == 0x65) //key frame need to add sps pps
            {
                outputStream.reset()
                outputStream.write(spsPpsInfo)
                outputStream.write(ret)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        val ret = outputStream.toByteArray()
        outputStream.reset()
        return ret
    }

    fun YV12toYUV420PackedSemiPlanar(
        input: ByteArray,
        output: ByteArray?,
        width: Int,
        height: Int
    ): ByteArray? {
        for (i in 0 until height) System.arraycopy(
            input,
            yStride * i,
            output,
            yStride * i,
            yStride
        ) // Y
        for (i in 0 until halfHeight) {
            for (j in 0 until halfWidth) {
                output!![ySize + (i * halfWidth + j) * 2] =
                    input[ySize + cSize + i * cStride + j] // Cb (U)
                output[ySize + (i * halfWidth + j) * 2 + 1] =
                    input[ySize + i * cStride + j] // Cr (V)
            }
        }
        return output
    }

    private fun computePresentationTime(frameIndex: Long, framerate: Int): Long {
        return 132 + frameIndex * 1000000 / framerate
    }

    companion object {
        private val TAG = AvcEncoder::class.java.simpleName
        private const val MIME_TYPE = "video/avc"
        private const val I_FRAME_INTERVAL = 1
        private fun selectCodec(mimeType: String): MediaCodecInfo? {
            val numCodecs = MediaCodecList.getCodecCount()
            for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                if (!codecInfo.isEncoder) continue
                val types = codecInfo.supportedTypes
                for (j in types.indices) {
                    if (types[j].equals(mimeType, ignoreCase = true)) return codecInfo
                }
            }
            return null
        }
    }
}