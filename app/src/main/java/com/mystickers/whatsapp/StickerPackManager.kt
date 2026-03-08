package com.mystickers.whatsapp

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File

/**
 * Manages sticker packs: scans the filesystem for .webp files,
 * groups them into packs of 30, processes images, and provides
 * access to the packs for the ContentProvider.
 */
class StickerPackManager(private val context: Context) {

    companion object {
        private const val TAG = "StickerPackManager"
        const val STICKERS_PER_PACK = 30
        private const val DEFAULT_PUBLISHER = "My Stickers"
        private const val PACK_PREFIX = "pack_"

        @Volatile
        private var instance: StickerPackManager? = null

        fun getInstance(context: Context): StickerPackManager {
            return instance ?: synchronized(this) {
                instance ?: StickerPackManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val stickerPacks = mutableListOf<StickerPack>()
    private var initialized = false

    /**
     * Returns all sticker packs. If not yet loaded, returns empty list.
     */
    fun getStickerPacks(): List<StickerPack> {
        return stickerPacks.toList()
    }

    /**
     * Returns a specific sticker pack by identifier.
     */
    fun getStickerPack(identifier: String): StickerPack? {
        return stickerPacks.find { it.identifier == identifier }
    }

    /**
     * Scans the given directory for .webp sticker files and organizes them
     * into packs of 30. Processes images (resize/compress) and generates
     * tray icons. This should be called on a background thread.
     *
     * @param sourceDir The directory to scan for .webp files
     * @param publisher The publisher name for the sticker packs
     * @param onProgress Callback for progress updates (current, total)
     * @return The number of sticker packs created
     */
    fun loadStickersFromDirectory(
        sourceDir: File,
        publisher: String = DEFAULT_PUBLISHER,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): Int {
        if (!sourceDir.exists() || !sourceDir.isDirectory) {
            Log.w(TAG, "Source directory does not exist or is not a directory: ${sourceDir.absolutePath}")
            return 0
        }

        // Find all .webp files recursively
        val webpFiles = sourceDir.walkTopDown()
            .filter { it.isFile && it.extension.equals("webp", ignoreCase = true) }
            .sortedBy { it.name }
            .toList()

        Log.i(TAG, "Found ${webpFiles.size} WebP files in ${sourceDir.absolutePath}")

        if (webpFiles.isEmpty()) return 0

        // Clear existing packs
        stickerPacks.clear()

        // Clean up old cache
        val cacheDir = ImageUtil.getStickerCacheDir(context)
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()

        // Group files into packs of STICKERS_PER_PACK
        val chunks = webpFiles.chunked(STICKERS_PER_PACK)
        val totalFiles = webpFiles.size
        var processedFiles = 0

        chunks.forEachIndexed { packIndex, files ->
            val packId = "${PACK_PREFIX}${packIndex + 1}"
            val packDir = ImageUtil.getStickerPackDir(context, packId)
            val stickers = mutableListOf<Sticker>()

            files.forEachIndexed { stickerIndex, sourceFile ->
                val stickerFileName = "sticker_${stickerIndex + 1}.webp"
                val processedFile = ImageUtil.processStickerImage(
                    context, sourceFile, "$packId/$stickerFileName"
                )

                if (processedFile != null) {
                    stickers.add(
                        Sticker(
                            imageFileName = stickerFileName,
                            emojis = listOf("😀"), // Default emoji
                            size = processedFile.length()
                        )
                    )
                } else {
                    Log.w(TAG, "Failed to process sticker: ${sourceFile.name}")
                }

                processedFiles++
                onProgress?.invoke(processedFiles, totalFiles)
            }

            if (stickers.isEmpty()) return@forEachIndexed

            // Generate tray icon from first sticker in pack
            val trayFileName = "tray_$packId.webp"
            val traySource = files.first()
            ImageUtil.createTrayIcon(context, traySource, "$packId/$trayFileName")

            val pack = StickerPack(
                identifier = packId,
                name = "$publisher Pack ${packIndex + 1}",
                publisher = publisher,
                trayImageFile = trayFileName,
                stickers = stickers
            )

            stickerPacks.add(pack)
        }

        initialized = true
        Log.i(TAG, "Created ${stickerPacks.size} sticker packs")
        return stickerPacks.size
    }

    /**
     * Scans common sticker directories on the device.
     */
    fun getDefaultStickerDirectories(): List<File> {
        val dirs = mutableListOf<File>()

        // Common locations where users might store stickers
        val externalStorage = Environment.getExternalStorageDirectory()
        val possiblePaths = listOf(
            File(externalStorage, "Stickers"),
            File(externalStorage, "stickers"),
            File(externalStorage, "WhatsApp Stickers"),
            File(externalStorage, "Download/Stickers"),
            File(externalStorage, "Downloads/Stickers"),
            File(externalStorage, "Pictures/Stickers"),
            File(externalStorage, "DCIM/Stickers")
        )

        possiblePaths.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                dirs.add(dir)
            }
        }

        return dirs
    }

    /**
     * Returns the file for a specific sticker in a pack.
     */
    fun getStickerFile(packIdentifier: String, stickerFileName: String): File {
        return File(ImageUtil.getStickerPackDir(context, packIdentifier), stickerFileName)
    }

    /**
     * Returns the tray icon file for a specific pack.
     */
    fun getTrayIconFile(packIdentifier: String): File? {
        val pack = getStickerPack(packIdentifier) ?: return null
        return File(ImageUtil.getStickerPackDir(context, packIdentifier), pack.trayImageFile)
    }

    /**
     * Returns the content URI for adding a sticker pack to WhatsApp.
     */
    fun getContentUri(packIdentifier: String): Uri {
        return Uri.Builder()
            .scheme("content")
            .authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            .appendPath("metadata")
            .appendPath(packIdentifier)
            .build()
    }

    /**
     * Check if sticker packs have been loaded.
     */
    fun isInitialized(): Boolean = initialized

    /**
     * Clears all loaded sticker packs and cache.
     */
    fun clear() {
        stickerPacks.clear()
        val cacheDir = ImageUtil.getStickerCacheDir(context)
        cacheDir.deleteRecursively()
        initialized = false
    }
}
