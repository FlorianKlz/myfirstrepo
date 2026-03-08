package com.example.stickerimporter.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.stickerimporter.model.StickerDescriptor
import com.example.stickerimporter.model.StickerPack
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StickerImporter(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
){
    private val storage = StickerStorage(context)

    suspend fun scanWebpCount(treeUri: Uri): Int = withContext(ioDispatcher) {
        val docTree = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext 0
        collectWebps(docTree).size
    }

    suspend fun prepareStickerPacks(
        treeUri: Uri,
        packBaseName: String,
        chunkSize: Int = 30
    ): List<StickerPack> = withContext(ioDispatcher) {
        val resolver = context.contentResolver
        val docTree = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val webps = collectWebps(docTree)
        if (webps.isEmpty()) return@withContext emptyList()

        val existingPacks = storage.loadStickerPacks().toMutableList()
        val createdPacks = mutableListOf<StickerPack>()
        val chunks = webps.chunked(chunkSize)
        val timestampVersion = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        chunks.forEachIndexed { index, chunk ->
            val identifier = storage.newPackIdentifier()
            val packDir = storage.getPackDirectory(identifier)
            packDir.mkdirs()

            val stickers = mutableListOf<StickerDescriptor>()
            var trayImageFile = "tray.webp"

            chunk.forEachIndexed { localIndex, documentFile ->
                val targetName = "sticker_${localIndex}.webp"
                val copied = storage.copyWebpIntoPack(packDir, documentFile, resolver, targetName)
                if (copied) {
                    stickers.add(StickerDescriptor(targetName))
                    if (localIndex == 0) {
                        storage.copyWebpIntoPack(packDir, documentFile, resolver, trayImageFile)
                    }
                }
            }

            if (stickers.isNotEmpty()) {
                val packName = "$packBaseName ${index + 1}"
                val pack = StickerPack(
                    identifier = identifier,
                    name = packName,
                    publisher = "Lokaler Import",
                    trayImageFile = trayImageFile,
                    stickers = stickers,
                    imageDataVersion = timestampVersion,
                    avoidCache = true
                )
                existingPacks.add(pack)
                createdPacks.add(pack)
            } else {
                packDir.deleteRecursively()
            }
        }

        storage.saveStickerPacks(existingPacks)
        createdPacks
    }

    private fun collectWebps(root: DocumentFile): List<DocumentFile> {
        val results = mutableListOf<DocumentFile>()
        val files = root.listFiles()
        files.forEach { file ->
            if (file.isDirectory) {
                results.addAll(collectWebps(file))
            } else if (file.isFile && file.isWebp()) {
                results.add(file)
            }
        }
        return results
    }

    fun existingPacks(): List<StickerPack> = storage.loadStickerPacks()

    fun clearAll() {
        storage.clearAll()
    }

    fun packAssetFile(packId: String, fileName: String): File? {
        val packDir = storage.getPackDirectory(packId)
        val candidate = File(packDir, fileName)
        return candidate.takeIf { it.exists() }
    }
}
