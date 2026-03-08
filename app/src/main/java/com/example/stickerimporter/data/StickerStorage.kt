package com.example.stickerimporter.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.stickerimporter.model.StickerDescriptor
import com.example.stickerimporter.model.StickerPack
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private const val MANIFEST_FILE = "manifest.json"
private const val PACKS_FOLDER = "sticker_packs"

class StickerStorage(private val context: Context) {

    fun loadStickerPacks(): List<StickerPack> {
        val manifest = File(getPacksRoot(), MANIFEST_FILE)
        if (!manifest.exists()) return emptyList()
        return try {
            val content = manifest.readText()
            val jsonArray = JSONArray(content)
            buildList {
                for (i in 0 until jsonArray.length()) {
                    parsePack(jsonArray.getJSONObject(i))?.let { add(it) }
                }
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun saveStickerPacks(packs: List<StickerPack>) {
        val manifest = File(getPacksRoot(), MANIFEST_FILE)
        manifest.parentFile?.mkdirs()
        val jsonArray = JSONArray()
        packs.forEach { pack ->
            jsonArray.put(pack.toJson())
        }
        manifest.writeText(jsonArray.toString())
    }

    fun getPackDirectory(identifier: String): File = File(getPacksRoot(), identifier)

    fun clearAll() {
        getPacksRoot().deleteRecursively()
    }

    fun copyWebpIntoPack(
        packDir: File,
        sourceFile: DocumentFile,
        resolver: android.content.ContentResolver,
        targetFileName: String
    ): Boolean {
        val input = resolver.openInputStream(sourceFile.uri) ?: return false
        val target = File(packDir, targetFileName)
        return try {
            FileOutputStream(target).use { out ->
                input.use { inp ->
                    inp.copyTo(out)
                }
            }
            true
        } catch (ioe: IOException) {
            false
        }
    }

    fun newPackIdentifier(): String = UUID.randomUUID().toString()

    private fun getPacksRoot(): File = File(context.filesDir, PACKS_FOLDER)

    private fun StickerPack.toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("identifier", identifier)
        obj.put("name", name)
        obj.put("publisher", publisher)
        obj.put("trayImageFile", trayImageFile)
        obj.put("imageDataVersion", imageDataVersion)
        obj.put("avoidCache", avoidCache)
        obj.put("publisherEmail", publisherEmail)
        obj.put("publisherWebsite", publisherWebsite)
        obj.put("privacyPolicyWebsite", privacyPolicyWebsite)
        obj.put("licenseAgreementWebsite", licenseAgreementWebsite)
        val stickersArray = JSONArray()
        stickers.forEach { sticker ->
            val stickerObj = JSONObject()
            stickerObj.put("file", sticker.fileName)
            stickerObj.put("emojis", JSONArray(sticker.emojis))
            stickersArray.put(stickerObj)
        }
        obj.put("stickers", stickersArray)
        return obj
    }

    private fun parsePack(obj: JSONObject): StickerPack? = try {
        val stickersArray = obj.optJSONArray("stickers") ?: JSONArray()
        val stickers = mutableListOf<StickerDescriptor>()
        for (i in 0 until stickersArray.length()) {
            val sObj = stickersArray.getJSONObject(i)
            val emojisJson = sObj.optJSONArray("emojis") ?: JSONArray()
            val emojis = mutableListOf<String>()
            for (j in 0 until emojisJson.length()) {
                emojis.add(emojisJson.getString(j))
            }
            stickers.add(StickerDescriptor(sObj.getString("file"), emojis))
        }
        StickerPack(
            identifier = obj.getString("identifier"),
            name = obj.getString("name"),
            publisher = obj.getString("publisher"),
            trayImageFile = obj.getString("trayImageFile"),
            stickers = stickers,
            imageDataVersion = obj.optString("imageDataVersion", "1"),
            avoidCache = obj.optBoolean("avoidCache", false),
            publisherEmail = obj.optString("publisherEmail", ""),
            publisherWebsite = obj.optString("publisherWebsite", ""),
            privacyPolicyWebsite = obj.optString("privacyPolicyWebsite", ""),
            licenseAgreementWebsite = obj.optString("licenseAgreementWebsite", "")
        )
    } catch (e: Exception) {
        null
    }
}

fun DocumentFile.isWebp(): Boolean {
    val name = this.name ?: return false
    return name.lowercase().endsWith(".webp")
}
