package com.void200.runku.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.void200.runku.data.Activity
import com.void200.runku.data.TrackPoint
import java.io.OutputStream

/**
 * Renders a Strava-style share card (canvas + metrics) into a JPG and saves it
 * to the device gallery (Pictures/runku). Fully offline — pure Canvas drawing.
 */
object ShareCard {

    fun render(
        activity: Activity,
        points: List<TrackPoint>,
        accentColor: Int = 0xFF3B6EF6.toInt(),
        bgColor: Int = 0xFF0C0E13.toInt(),
        textColor: Int = 0xFFEEF1F6.toInt(),
        mutedColor: Int = 0xFF8B93A1.toInt(),
    ): Bitmap {
        val w = 1080; val h = 1920
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // background
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), bgColor, blendDark(bgColor, 0.6f), Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        p.shader = null

        // ---- route panel ----
        val panel = RectF(60f, 180f, w - 60f, 1180f)
        p.color = blendDark(bgColor, 1.15f)
        cv.drawRoundRect(panel, 44f, 44f, p)

        if (points.size >= 2) {
            var minLat = points[0].lat; var maxLat = points[0].lat
            var minLon = points[0].lon; var maxLon = points[0].lon
            for (t in points) {
                if (t.lat < minLat) minLat = t.lat; if (t.lat > maxLat) maxLat = t.lat
                if (t.lon < minLon) minLon = t.lon; if (t.lon > maxLon) maxLon = t.lon
            }
            val spanLat = (maxLat - minLat).coerceAtLeast(1e-5)
            val spanLon = (maxLon - minLon).coerceAtLeast(1e-5)
            val padPx = 90f
            val iw = panel.width() - padPx * 2
            val ih = panel.height() - padPx * 2

            val path = android.graphics.Path()
            points.forEachIndexed { i, t ->
                val fx = ((t.lon - minLon) / spanLon).toFloat()
                val fy = ((t.lat - minLat) / spanLat).toFloat()
                val x = panel.left + padPx + fx * iw
                val y = panel.bottom - padPx - fy * ih
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            p.style = Paint.Style.STROKE
            p.strokeWidth = 16f
            p.strokeCap = Paint.Cap.ROUND
            p.strokeJoin = Paint.Join.ROUND
            p.color = accentColor
            cv.drawPath(path, p)
            p.style = Paint.Style.FILL
        }

        // ---- brand ----
        p.color = accentColor
        p.textSize = 44f
        p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        cv.drawText("runku", 70f, 130f, p)

        // ---- big distance ----
        p.color = textColor
        p.textSize = 190f
        p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val dist = "%.2f".format(activity.distanceMeters / 1000.0)
        cv.drawText(dist, 70f, 1380f, p)
        val dw = p.measureText(dist)
        p.textSize = 60f
        p.color = mutedColor
        cv.drawText("km", 70f + dw + 16f, 1380f, p)

        p.textSize = 46f
        p.color = textColor
        p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        cv.drawText(
            java.text.SimpleDateFormat("d MMMM yyyy · HH:mm", java.util.Locale("id"))
                .format(java.util.Date(activity.startTime)),
            70f, 1460f, p
        )

        // ---- metric row ----
        val metrics = listOf(
            "Pace" to formatPace(activity.avgPaceSecPerKm),
            "Waktu" to formatDuration(activity.durationMillis),
            "Elevasi" to "+${activity.elevationGainM.toInt()}m",
        )
        var x = 70f
        val y = 1640f
        metrics.forEach { (k, v) ->
            p.color = mutedColor; p.textSize = 34f
            cv.drawText(k.uppercase(), x, y, p)
            p.color = textColor; p.textSize = 62f
            p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            cv.drawText(v, x, y + 74f, p)
            p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            x += (w - 140f) / 3
        }

        // footer
        p.color = mutedColor; p.textSize = 30f
        cv.drawText("runku · oleh prawira dwimadya", 70f, 1840f, p)

        return bmp
    }

    /** Save to gallery (Pictures/runku). Returns true on success. */
    fun saveToGallery(context: Context, bmp: Bitmap, name: String): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/runku")
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        return runCatching {
            resolver.openOutputStream(uri)?.use { out: OutputStream ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            true
        }.getOrDefault(false)
    }

    private fun blendDark(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun formatDuration(millis: Long): String {
        val total = millis / 1000
        val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    private fun formatPace(secPerKm: Double): String {
        if (secPerKm <= 0 || secPerKm.isInfinite()) return "--'--\""
        return "%d'%02d\"".format(secPerKm.toInt() / 60, secPerKm.toInt() % 60)
    }
}
