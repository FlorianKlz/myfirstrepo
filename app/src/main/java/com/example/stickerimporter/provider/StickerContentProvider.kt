package com.example.stickerimporter.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.stickerimporter.data.StickerImporter
import com.example.stickerimporter.model.StickerPack
import java.io.File
import java.io.FileNotFoundException

class StickerContentProvider : ContentProvider() {

    private lateinit var importer: StickerImporter
    private lateinit var authority: String
    private lateinit var uriMatcher: UriMatcher

    private val metadataColumns = arrayOf(
        "_id",
        "identifier",
        "name",
        "publisher",
        "tray_image_file",
        "publisher_email",
        "publisher_website",
        "privacy_policy_website",
        "license_agreement_website",
        "image_data_version",
        "avoid_cache"
    )

    private val stickerColumns = arrayOf(
        "_id",
        "sticker_file",
        "emojis"
    )

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        authority = "${ctx.packageName}.stickercontentprovider"
        importer = StickerImporter(ctx)
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, "metadata", METADATA)
            addURI(authority, "metadata/*", METADATA_FOR_PACK)
            addURI(authority, "stickers/*", STICKERS_FOR_PACK)
            addURI(authority, "stickers_asset/*/*", STICKER_ASSET)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val match = uriMatcher.match(uri)
        val packs = importer.existingPacks()
        return when (match) {
            METADATA -> buildMetadataCursor(packs)
            METADATA_FOR_PACK -> {
                val identifier = uri.lastPathSegment ?: return null
                buildMetadataCursor(packs.filter { it.identifier == identifier })
            }
            STICKERS_FOR_PACK -> {
                val identifier = uri.lastPathSegment ?: return null
                val pack = packs.firstOrNull { it.identifier == identifier } ?: return null
                buildStickersCursor(pack)
            }
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            METADATA, METADATA_FOR_PACK -> "vnd.android.cursor.dir/vnd.$authority.metadata"
            STICKERS_FOR_PACK -> "vnd.android.cursor.dir/vnd.$authority.sticker"
            STICKER_ASSET -> "image/webp"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val match = uriMatcher.match(uri)
        if (match != STICKER_ASSET) throw FileNotFoundException("Unsupported uri $uri")
        val segments = uri.pathSegments
        if (segments.size < 3) throw FileNotFoundException("Malformed uri $uri")
        val packId = segments[1]
        val fileName = segments[2]
        val file = importer.packAssetFile(packId, fileName)
            ?: throw FileNotFoundException("File not found for $uri")
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun buildMetadataCursor(packs: List<StickerPack>): Cursor {
        val cursor = MatrixCursor(metadataColumns)
        packs.forEachIndexed { index, pack ->
            cursor.addRow(
                arrayOf(
                    index,
                    pack.identifier,
                    pack.name,
                    pack.publisher,
                    pack.trayImageFile,
                    pack.publisherEmail,
                    pack.publisherWebsite,
                    pack.privacyPolicyWebsite,
                    pack.licenseAgreementWebsite,
                    pack.imageDataVersion,
                    if (pack.avoidCache) 1 else 0
                )
            )
        }
        return cursor
    }

    private fun buildStickersCursor(pack: StickerPack): Cursor {
        val cursor = MatrixCursor(stickerColumns)
        pack.stickers.forEachIndexed { index, sticker ->
            cursor.addRow(
                arrayOf(
                    index,
                    sticker.fileName,
                    sticker.emojis.joinToString(",")
                )
            )
        }
        return cursor
    }

    companion object {
        private const val METADATA = 1
        private const val METADATA_FOR_PACK = 2
        private const val STICKERS_FOR_PACK = 3
        private const val STICKER_ASSET = 4
    }
}
