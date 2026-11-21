package com.example.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.data.model.ActiveEditProjectState
import com.example.data.model.AspectRatioOption
import com.example.data.model.DeviceVideoCapabilities
import com.example.data.model.QualityPreset
import com.example.data.model.SourceMediaInfo
import com.example.data.model.WatermarkConfig
import com.example.data.model.WatermarkMode
import com.example.data.model.WatermarkPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val TAG = "MediaProcessingEngine"

class MediaProcessingEngine(private val context: Context) {

    fun queryDeviceVideoCapabilities(): DeviceVideoCapabilities {
        var maxWidth = 1920
        var maxHeight = 1080
        var encoderName = "Encoder H.264/AVC"
        try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            for (info in codecList.codecInfos) {
                if (!info.isEncoder) continue
                val types = info.supportedTypes
                val supportsAvc = types.any { it.equals(MediaFormat.MIMETYPE_VIDEO_AVC, ignoreCase = true) }
                val supportsHevc = types.any { it.equals(MediaFormat.MIMETYPE_VIDEO_HEVC, ignoreCase = true) }
                if (!supportsAvc && !supportsHevc) continue

                val mime = if (supportsAvc) MediaFormat.MIMETYPE_VIDEO_AVC else MediaFormat.MIMETYPE_VIDEO_HEVC
                val caps = info.getCapabilitiesForType(mime).videoCapabilities ?: continue
                val wRange = caps.supportedWidths
                val hRange = caps.supportedHeights
                if (wRange.upper >= maxWidth && hRange.upper >= maxHeight) {
                    maxWidth = wRange.upper
                    maxHeight = hRange.upper
                    encoderName = info.name
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to query MediaCodecList, using conservative defaults", e)
        }
        val longSide = max(maxWidth, maxHeight)
        val shortSide = min(maxWidth, maxHeight)
        return DeviceVideoCapabilities(
            maxSupportedWidth = maxWidth,
            maxSupportedHeight = maxHeight,
            supports2K = longSide >= 2560 && shortSide >= 1440,
            supports4K = longSide >= 3840 && shortSide >= 2160,
            hardwareEncoderName = encoderName,
            stabilizationAvailable = true
        )
    }

    suspend fun inspectMediaUri(uri: Uri): SourceMediaInfo = withContext(Dispatchers.IO) {
        val mime = context.contentResolver.getType(uri).orEmpty()
        val displayName = resolveDisplayName(uri)
        val isImage = mime.startsWith("image/")
        if (isImage) {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, opts)
            }
            return@withContext SourceMediaInfo(
                uri = uri,
                isImage = true,
                width = opts.outWidth.takeIf { it > 0 } ?: 1080,
                height = opts.outHeight.takeIf { it > 0 } ?: 1920,
                durationMs = 4_000L,
                displayLabel = displayName
            )
        }

        val retriever = MediaMetadataRetriever()
        return@withContext try {
            retriever.setDataSource(context, uri)
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 1080
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1920
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 10_000L
            SourceMediaInfo(
                uri = uri,
                isImage = false,
                width = width,
                height = height,
                durationMs = durationMs,
                displayLabel = displayName
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to inspect video metadata", e)
            SourceMediaInfo(
                uri = uri,
                isImage = false,
                width = 1080,
                height = 1920,
                durationMs = 10_000L,
                displayLabel = displayName
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    suspend fun extractSampleFrames(uri: Uri, maxFrames: Int = 4): List<Bitmap> = withContext(Dispatchers.IO) {
        val mime = context.contentResolver.getType(uri).orEmpty()
        if (mime.startsWith("image/")) {
            val bmp = loadScaledBitmap(uri, 768)
            return@withContext if (bmp != null) listOf(bmp) else emptyList()
        }

        val frames = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 8_000L
            val count = maxFrames.coerceIn(1, 6)
            for (i in 0 until count) {
                val timeUs = ((durationMs * 1000L) * (i + 0.5) / count).toLong().coerceAtLeast(0L)
                val raw = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (raw != null) {
                    frames.add(scaleDownBitmap(raw, 720))
                    if (frames.last() !== raw) {
                        raw.recycle()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Frame extraction failed", e)
        } finally {
            runCatching { retriever.release() }
        }
        frames
    }

    suspend fun saveBitmapToGallery(bitmap: Bitmap, fileNamePrefix: String): Result<Uri> = withContext(Dispatchers.IO) {
        return@withContext try {
            val name = "${fileNamePrefix}_${System.currentTimeMillis()}.png"
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ChoszenEditAI")
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Não foi possível criar arquivo na Galeria.")
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: throw IllegalStateException("Não foi possível gravar a imagem.")
            Result.success(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving bitmap to gallery", e)
            Result.failure(e)
        }
    }

    fun computeTargetDimensions(
        aspectRatio: AspectRatioOption,
        requestedQuality: QualityPreset,
        sourceInfo: SourceMediaInfo?,
        capabilities: DeviceVideoCapabilities
    ): Pair<Pair<Int, Int>, String?> {
        // Never invent resolution unsupported by hardware encoder or claim upscaling beyond truth
        val effectiveQuality = when {
            requestedQuality == QualityPreset.UHD_4K && !capabilities.supports4K -> QualityPreset.FULL_HD
            requestedQuality == QualityPreset.QHD_2K && !capabilities.supports2K -> QualityPreset.FULL_HD
            else -> requestedQuality
        }

        val (targetW, targetH) = when (aspectRatio) {
            AspectRatioOption.VERTICAL_9_16 -> effectiveQuality.shortSidePx to effectiveQuality.longSidePx
            AspectRatioOption.SQUARE_1_1 -> effectiveQuality.shortSidePx to effectiveQuality.shortSidePx
            AspectRatioOption.HORIZONTAL_16_9 -> effectiveQuality.longSidePx to effectiveQuality.shortSidePx
        }

        val warning = buildList {
            if (effectiveQuality != requestedQuality) {
                add("O hardware deste dispositivo suporta até ${effectiveQuality.label}; exportado em ${effectiveQuality.label} real.")
            }
            if (sourceInfo != null) {
                val srcMin = min(sourceInfo.width, sourceInfo.height)
                if (effectiveQuality.shortSidePx > srcMin) {
                    add("Clipe original tem ${sourceInfo.width}x${sourceInfo.height}; reamostragem preservando a fidelidade da fonte sem inventar detalhes inexistentes.")
                }
            }
        }.takeIf { it.isNotEmpty() }?.joinToString(" ")

        return (targetW to targetH) to warning
    }

    @OptIn(UnstableApi::class)
    fun buildPreviewVideoEffects(
        state: ActiveEditProjectState,
        targetWidth: Int,
        targetHeight: Int
    ): List<Effect> {
        val effects = mutableListOf<Effect>()

        // 1. Contrast & Sharpness boost
        val effectiveContrast = (state.contrast + if (state.sharpnessBoost) 0.08f else 0f).coerceIn(-0.9f, 0.95f)
        if (effectiveContrast != 0f) {
            effects.add(Contrast(effectiveContrast))
        }

        // 2. Color & Lighting enhancement
        val satPercent = ((state.saturation + if (state.colorAdjustment) 0.10f else 0f) * 100f).coerceIn(-100f, 100f)
        val lightPercent = ((state.brightness + if (state.lightingEnhance) 0.04f else 0f) * 100f).coerceIn(-100f, 100f)
        if (satPercent != 0f || lightPercent != 0f) {
            effects.add(
                HslAdjustment.Builder()
                    .adjustSaturation(satPercent)
                    .adjustLightness(lightPercent)
                    .build()
            )
        }

        if (state.lightingEnhance || state.glowIntensity > 0.4f) {
            val boost = 1f + (state.glowIntensity * 0.08f)
            effects.add(
                RgbAdjustment.Builder()
                    .setRedScale(boost)
                    .setGreenScale(boost)
                    .setBlueScale(boost + 0.02f)
                    .build()
            )
        }

        // 3. Stabilization crop margin / zoom motion
        val zoomScale = 1f + (if (state.stabilizationEnabled) 0.06f else 0f) + (state.zoomIntensity * 0.08f)
        if (zoomScale > 1.01f) {
            effects.add(
                ScaleAndRotateTransformation.Builder()
                    .setScale(zoomScale, zoomScale)
                    .build()
            )
        }

        // 4. Target aspect ratio & resolution
        effects.add(
            Presentation.createForWidthAndHeight(
                targetWidth,
                targetHeight,
                Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP
            )
        )

        // 5. Watermark & caption overlay
        if (state.watermarkConfig.enabled || state.captionText.isNotBlank()) {
            val logoBmp = state.watermarkConfig.imageUri?.let { loadScaledBitmap(it, 320) }
            val overlayBmp = renderOverlayBitmap(
                watermark = state.watermarkConfig,
                logoBitmap = logoBmp,
                captionText = state.captionText,
                width = targetWidth,
                height = targetHeight
            )
            effects.add(
                OverlayEffect(
                    listOf(BitmapOverlay.createStaticBitmapOverlay(overlayBmp))
                )
            )
        }

        return effects
    }

    @OptIn(UnstableApi::class)
    fun startExport(
        state: ActiveEditProjectState,
        capabilities: DeviceVideoCapabilities,
        onProgress: (Int, String) -> Unit,
        onCompleted: (File, Uri, Long, String, String?) -> Unit,
        onError: (String) -> Unit
    ): Transformer? {
        if (state.mediaItems.isEmpty()) {
            onError("Selecione ao menos um vídeo ou imagem antes de exportar.")
            return null
        }

        val firstSource = state.mediaItems.first()
        val (dimensions, warning) = computeTargetDimensions(
            aspectRatio = state.aspectRatio,
            requestedQuality = state.qualityPreset,
            sourceInfo = firstSource,
            capabilities = capabilities
        )
        val (targetW, targetH) = dimensions

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(exportDir, "choszen_edit_${System.currentTimeMillis()}.mp4")
        if (outputFile.exists()) outputFile.delete()

        val videoEffects = buildPreviewVideoEffects(state, targetW, targetH)
        val editedItems = state.mediaItems.map { source ->
            val mediaItemBuilder = MediaItem.Builder().setUri(source.uri)
            if (source.isImage) {
                mediaItemBuilder.setImageDurationMs(4_000L)
            }
            val itemBuilder = EditedMediaItem.Builder(mediaItemBuilder.build())
                .setEffects(Effects(emptyList(), videoEffects))
            if (source.isImage) {
                itemBuilder.setFrameRate(30)
            }
            if (state.musicUri != null) {
                itemBuilder.setRemoveAudio(true)
            }
            itemBuilder.build()
        }

        val videoSequence = EditedMediaItemSequence.Builder(editedItems).build()
        val compositionBuilder = Composition.Builder(
            buildList {
                add(videoSequence)
                val musicUri = state.musicUri
                if (musicUri != null) {
                    val audioItem = EditedMediaItem.Builder(MediaItem.fromUri(musicUri))
                        .setRemoveVideo(true)
                        .build()
                    add(EditedMediaItemSequence.Builder(listOf(audioItem)).build())
                }
            }
        )

        val mainHandler = Handler(Looper.getMainLooper())
        var activeTransformer: Transformer? = null
        val progressRunnable = object : Runnable {
            val holder = ProgressHolder()
            override fun run() {
                val t = activeTransformer ?: return
                val progressState = t.getProgress(holder)
                if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onProgress(holder.progress, "Renderizando MP4 (${targetW}x${targetH})...")
                }
                if (progressState != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    mainHandler.postDelayed(this, 350L)
                }
            }
        }

        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                mainHandler.removeCallbacks(progressRunnable)
                val galleryUri = copyMp4ToMediaStore(outputFile) ?: Uri.fromFile(outputFile)
                val sizeBytes = outputFile.length().takeIf { it > 0L } ?: exportResult.fileSizeBytes
                onCompleted(
                    outputFile,
                    galleryUri,
                    sizeBytes,
                    "${targetW}x${targetH} MP4",
                    warning
                )
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                mainHandler.removeCallbacks(progressRunnable)
                Log.e(TAG, "Media3 Transformer export failed", exportException)
                onError(exportException.localizedMessage ?: "Falha ao exportar vídeo MP4.")
            }
        }

        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .addListener(listener)
            .build()
        activeTransformer = transformer

        onProgress(0, "Preparando composição Media3...")
        transformer.start(compositionBuilder.build(), outputFile.absolutePath)
        mainHandler.postDelayed(progressRunnable, 350L)
        return transformer
    }

    private fun copyMp4ToMediaStore(sourceFile: File): Uri? {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, sourceFile.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ChoszenEditAI")
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(out)
                }
            }
            uri
        } catch (e: Exception) {
            Log.w(TAG, "Unable to publish MP4 to MediaStore Movies", e)
            null
        }
    }

    private fun renderOverlayBitmap(
        watermark: WatermarkConfig,
        logoBitmap: Bitmap?,
        captionText: String,
        width: Int,
        height: Int
    ): Bitmap {
        val safeW = width.coerceAtLeast(360)
        val safeH = height.coerceAtLeast(360)
        val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val margin = (safeW * 0.045f).coerceAtLeast(24f)

        if (watermark.enabled) {
            val alphaInt = (watermark.opacity.coerceIn(0.1f, 1f) * 255).roundToInt()
            if (watermark.mode == WatermarkMode.IMAGE && logoBitmap != null) {
                val targetLogoW = (safeW * 0.18f * watermark.sizeScale).roundToInt().coerceAtLeast(48)
                val aspect = logoBitmap.height.toFloat() / logoBitmap.width.coerceAtLeast(1)
                val targetLogoH = (targetLogoW * aspect).roundToInt().coerceAtLeast(32)
                val scaled = Bitmap.createScaledBitmap(logoBitmap, targetLogoW, targetLogoH, true)
                val (left, top) = computeAnchorOrigin(
                    position = watermark.position,
                    canvasW = safeW.toFloat(),
                    canvasH = safeH.toFloat(),
                    itemW = targetLogoW.toFloat(),
                    itemH = targetLogoH.toFloat(),
                    margin = margin
                )
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = alphaInt }
                canvas.drawBitmap(scaled, left, top, paint)
                if (scaled !== logoBitmap) scaled.recycle()
            } else {
                val label = watermark.text.ifBlank { "CHOSZEN™©" }
                val textSizePx = (safeW * 0.042f * watermark.sizeScale).coerceIn(20f, 140f)
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(alphaInt, 255, 255, 255)
                    textSize = textSizePx
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    if (watermark.shadowEnabled) {
                        setShadowLayer(6f, 2f, 3f, Color.argb(alphaInt, 0, 0, 0))
                    }
                }
                val bounds = Rect()
                fillPaint.getTextBounds(label, 0, label.length, bounds)
                val (left, top) = computeAnchorOrigin(
                    position = watermark.position,
                    canvasW = safeW.toFloat(),
                    canvasH = safeH.toFloat(),
                    itemW = bounds.width().toFloat(),
                    itemH = bounds.height().toFloat(),
                    margin = margin
                )
                val baselineY = top + bounds.height()
                if (watermark.outlineEnabled) {
                    val strokePaint = Paint(fillPaint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (textSizePx * 0.12f).coerceAtLeast(2f)
                        color = Color.argb(alphaInt, 7, 9, 15)
                        clearShadowLayer()
                    }
                    canvas.drawText(label, left, baselineY, strokePaint)
                }
                canvas.drawText(label, left, baselineY, fillPaint)
            }
        }

        if (captionText.isNotBlank()) {
            val capSize = (safeW * 0.048f).coerceIn(22f, 96f)
            val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(200, 255, 46)
                textSize = capSize
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setShadowLayer(8f, 0f, 3f, Color.BLACK)
            }
            val strokeCap = Paint(capPaint).apply {
                style = Paint.Style.STROKE
                strokeWidth = 5f
                color = Color.BLACK
                clearShadowLayer()
            }
            val capY = safeH * 0.80f
            canvas.drawText(captionText.take(44), safeW / 2f, capY, strokeCap)
            canvas.drawText(captionText.take(44), safeW / 2f, capY, capPaint)
        }

        return bitmap
    }

    private fun computeAnchorOrigin(
        position: WatermarkPosition,
        canvasW: Float,
        canvasH: Float,
        itemW: Float,
        itemH: Float,
        margin: Float
    ): Pair<Float, Float> {
        val x = when (position) {
            WatermarkPosition.TOP_LEFT,
            WatermarkPosition.CENTER_LEFT,
            WatermarkPosition.BOTTOM_LEFT -> margin
            WatermarkPosition.TOP_CENTER,
            WatermarkPosition.CENTER,
            WatermarkPosition.BOTTOM_CENTER -> (canvasW - itemW) / 2f
            WatermarkPosition.TOP_RIGHT,
            WatermarkPosition.CENTER_RIGHT,
            WatermarkPosition.BOTTOM_RIGHT -> canvasW - itemW - margin
        }
        val y = when (position) {
            WatermarkPosition.TOP_LEFT,
            WatermarkPosition.TOP_CENTER,
            WatermarkPosition.TOP_RIGHT -> margin
            WatermarkPosition.CENTER_LEFT,
            WatermarkPosition.CENTER,
            WatermarkPosition.CENTER_RIGHT -> (canvasH - itemH) / 2f
            WatermarkPosition.BOTTOM_LEFT,
            WatermarkPosition.BOTTOM_CENTER,
            WatermarkPosition.BOTTOM_RIGHT -> canvasH - itemH - margin
        }
        return x.coerceAtLeast(8f) to y.coerceAtLeast(8f)
    }

    private fun loadScaledBitmap(uri: Uri, maxDim: Int): Bitmap? {
        return try {
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null
            val scaled = scaleDownBitmap(raw, maxDim)
            if (scaled !== raw) raw.recycle()
            scaled
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode bitmap", e)
            null
        }
    }

    private fun scaleDownBitmap(source: Bitmap, maxDim: Int): Bitmap {
        val maxSide = max(source.width, source.height)
        if (maxSide <= maxDim) return source
        val ratio = maxDim.toFloat() / maxSide.toFloat()
        val w = (source.width * ratio).roundToInt().coerceAtLeast(1)
        val h = (source.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, w, h, true)
    }

    private fun resolveDisplayName(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) return cursor.getString(idx)
                    }
                }
            uri.lastPathSegment ?: "Mídia"
        } catch (_: Exception) {
            "Mídia"
        }
    }
}
