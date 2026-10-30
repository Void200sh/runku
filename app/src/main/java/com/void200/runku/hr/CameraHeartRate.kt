package com.void200.runku.hr

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executors

/**
 * Camera-based heart-rate (PPG). Place a fingertip over the REAR camera lens
 * (with the flash on). The red channel pulses with each heartbeat; we detect
 * peaks on a rolling signal and emit BPM.
 *
 * No Bluetooth, no strap — only the CAMERA permission is needed.
 */
class CameraHeartRate(private val context: Context) {

    fun stream(lifecycleOwner: LifecycleOwner): Flow<Int> = callbackFlow {
        val channel = this
        val executor = Executors.newSingleThreadExecutor()
        val providerFuture = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null
        var analysis: ImageAnalysis? = null

        val detector = PeakDetector { bpm -> channel.trySend(bpm) }

        providerFuture.addListener({
            try {
                provider = providerFuture.get()
                val analyzer = ImageAnalysis.Analyzer { image: ImageProxy ->
                    try {
                        detector.onFrame(image)
                    } finally {
                        image.close()
                    }
                }
                val analysisUseCase = ImageAnalysis.Builder()
                    .setTargetResolution(Size(320, 240))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(executor, analyzer) }
                analysis = analysisUseCase

                val sel = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                provider?.unbindAll()
                provider?.bindToLifecycle(lifecycleOwner, sel, analysisUseCase)
                setTorch(true)
            } catch (e: Exception) {
                channel.close(e)
            }
        }, ContextCompat.getMainExecutor(context))

        awaitClose {
            setTorch(false)
            runCatching { provider?.unbind(analysis) }
            executor.shutdown()
        }
    }

    /** Enable/disable the torch (flash) for a given camera. */
    fun setTorch(on: Boolean) {
        runCatching {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val id = cm.cameraIdList.firstOrNull {
                cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return
            cm.setTorchMode(id, on)
        }
    }

    /**
     * Very small peak detector: tracks mean red intensity over a short window,
     * counts upward zero-crossings of the AC component, and converts to BPM.
     */
    private class PeakDetector(private val onBpm: (Int) -> Unit) {
        private val window = ArrayDeque<Float>()
        private val windowSize = 90           // ~3s at ~30fps
        private var lastBeatMs = 0L
        private var aboveMean = false
        private var smoothed = 0f
        private var lastEmit = 0L
        private val intervals = ArrayDeque<Long>()

        fun onFrame(image: ImageProxy) {
            // Sample luma from the first (Y) plane; cheap and does not need
            // color conversion. Fingertip + flash gives a strong AC pulse.
            val buf = image.planes[0].buffer
            buf.rewind()
            var sum = 0L
            var n = 0
            while (buf.hasRemaining() && n < 500) {
                sum += (buf.get().toInt() and 0xFF)
                n++
            }
            val lum = if (n > 0) sum.toFloat() / n else 0f
            if (lum > 0f) process(lum, System.currentTimeMillis())
        }

        private fun process(v: Float, nowMs: Long) {
            // low-pass smoothing
            smoothed = if (smoothed == 0f) v else smoothed * 0.8f + v * 0.2f
            window.addLast(smoothed)
            if (window.size > windowSize) window.removeFirst()
            if (window.size < windowSize) return

            val mean = window.average().toFloat()
            val signal = smoothed - mean

            if (signal > 0 && !aboveMean) {
                aboveMean = true
                if (lastBeatMs != 0L) {
                    val interval = nowMs - lastBeatMs
                    if (interval in 300..2000) {   // 30–200 bpm
                        intervals.addLast(interval)
                        if (intervals.size > 5) intervals.removeFirst()
                        val bpm = (60000.0 / intervals.average()).toInt()
                        if (nowMs - lastEmit > 1500) {
                            lastEmit = nowMs
                            onBpm(bpm.coerceIn(35, 210))
                        }
                    }
                }
                lastBeatMs = nowMs
            } else if (signal < -2f) {
                aboveMean = false
            }
        }
    }
}
