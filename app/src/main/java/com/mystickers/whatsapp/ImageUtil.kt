package com.mystickers.whatsapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for processing sticker images.
 * Handles resizing stickers to 512x512 and generating 96x96 tray icons.
 */
object ImageUtil {

    const val STICKER_WIDTH = 512
    const val STICKER_HEIGHT = 512
    const val TRAY_IMAGE_WIDTH = 96
    const val TRAY_IMAGE_HEIGHT = 96
    const val MAX_STICKER_FILE_SIZE = 100 * 1024L // 100 KB
    const val MAX_TRAY_FILE_SIZE = 50 * 1024L // 50 KB

    /**
     * Creates a tray icon (96x96 WebP) from the given source sticker file.
     * Returns the path to the created tray icon file, or null on failure.
     */
    fun createTrayIcon(context: Context, sourceFile: File, outputFileName: String): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return null
            val scaled = Bitmap.createScaledBitmap(bitmap, TRAY_IMAGE_WIDTH, TRAY_IMAGE_HEIGHT, true)

            val outputFile = File(getStickerCacheDir(context), outputFileName)
            var quality = 80
            var bytes: ByteArray

            // Compress with decreasing quality until under MAX_TRAY_FILE_SIZE
            do {
                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.WEBP, quality, baos)
                bytes = baos.toByteArray()
                quality -= 10
            } while (bytes.size > MAX_TRAY_FILE_SIZE && quality > 0)

            FileOutputStream(outputFile).use { it.write(bytes) }
            bitmap.recycle()
            scaled.recycle()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Processes a sticker image: ensures it is 512x512 WebP and under 100KB.
     * Copies the processed file to the cache directory.
     * Returns the processed file, or null on failure.
     */
    fun processStickerImage(context: Context, sourceFile: File, outputFileName: String): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return null

            // If already correct size and file is small enough, just copy
            if (bitmap.width == STICKER_WIDTH && bitmap.height == STICKER_HEIGHT
                && sourceFile.length() <= MAX_STICKER_FILE_SIZE
            ) {
                val outputFile = File(getStickerCacheDir(context), outputFileName)
                sourceFile.copyTo(outputFile, overwrite = true)
                bitmap.recycle()
                return outputFile
            }

            // Resize to 512x512
            val scaled = Bitmap.createScaledBitmap(bitmap, STICKER_WIDTH, STICKER_HEIGHT, true)

            val outputFile = File(getStickerCacheDir(context), outputFileName)
            var quality = 90
            var bytes: ByteArray

            // Compress with decreasing quality until under MAX_STICKER_FILE_SIZE
            do {
                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.WEBP, quality, baos)
                bytes = baos.toByteArray()
                quality -= 5
            } while (bytes.size > MAX_STICKER_FILE_SIZE && quality > 0)

            FileOutputStream(outputFile).use { it.write(bytes) }
            bitmap.recycle()
            scaled.recycle()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns (or creates) the sticker cache directory for a specific pack.
     */
    fun getStickerPackDir(context: Context, packIdentifier: String): File {
        val dir = File(getStickerCacheDir(context), packIdentifier)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Returns (or creates) the base sticker cache directory.
     */
    fun getStickerCacheDir(context: Context): File {
        val dir = File(context.filesDir, "stickers")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
