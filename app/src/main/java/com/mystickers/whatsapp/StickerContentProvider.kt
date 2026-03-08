package com.mystickers.whatsapp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File

/**
 * ContentProvider that serves sticker packs to WhatsApp.
 * Implements the WhatsApp Sticker API protocol.
 *
 * URI patterns:
 *   /metadata                              - List all sticker packs
 *   /metadata/{pack_id}                    - Details of a specific pack
 *   /stickers/{pack_id}                    - Stickers in a pack
 *   /stickers_asset/{pack_id}/{file_name}  - Actual sticker file data
 */
class StickerContentProvider : ContentProvider() {

    companion object {
        private const val TAG = "StickerContentProvider"

        // URI match codes
        private const val METADATA_CODE = 1
        private const val METADATA_CODE_FOR_SINGLE_PACK = 2
        private const val STICKERS_CODE = 3
        private const val STICKERS_ASSET_CODE = 4

        // Metadata columns
        const val STICKER_PACK_IDENTIFIER = "sticker_pack_identifier"
        const val STICKER_PACK_NAME = "sticker_pack_name"
        const val STICKER_PACK_PUBLISHER = "sticker_pack_publisher"
        const val STICKER_PACK_ICON = "sticker_pack_icon"
        const val ANDROID_APP_DOWNLOAD_LINK = "android_play_store_link"
        const val IOS_APP_DOWNLOAD_LINK = "ios_app_download_link"
        const val PUBLISHER_EMAIL = "sticker_pack_publisher_email"
        const val PUBLISHER_WEBSITE = "sticker_pack_publisher_website"
        const val PRIVACY_POLICY_WEBSITE = "sticker_pack_privacy_policy_website"
        const val LICENSE_AGREEMENT_WEBSITE = "sticker_pack_license_agreement_website"
        const val IMAGE_DATA_VERSION = "image_data_version"
        const val AVOID_CACHE = "whatsapp_will_not_cache_stickers"
        const val ANIMATED_STICKER_PACK = "animated_sticker_pack"

        // Sticker columns
        const val STICKER_FILE_NAME = "sticker_file_name_in_query"
        const val STICKER_FILE_EMOJI = "sticker_emoji"
    }

    private lateinit var authority: String
    private lateinit var uriMatcher: UriMatcher

    override fun onCreate(): Boolean {
        authority = BuildConfig.CONTENT_PROVIDER_AUTHORITY
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, "metadata", METADATA_CODE)
            addURI(authority, "metadata/*", METADATA_CODE_FOR_SINGLE_PACK)
            addURI(authority, "stickers/*", STICKERS_CODE)
            addURI(authority, "stickers_asset/*/*", STICKERS_ASSET_CODE)
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
        val ctx = context ?: return null
        val manager = StickerPackManager.getInstance(ctx)

        return when (uriMatcher.match(uri)) {
            METADATA_CODE -> {
                getPacksCursor(manager.getStickerPacks())
            }
            METADATA_CODE_FOR_SINGLE_PACK -> {
                val packId = uri.lastPathSegment ?: return null
                val pack = manager.getStickerPack(packId) ?: return null
                getPacksCursor(listOf(pack))
            }
            STICKERS_CODE -> {
                val packId = uri.lastPathSegment ?: return null
                val pack = manager.getStickerPack(packId) ?: return null
                getStickersCursor(pack)
            }
            else -> {
                Log.w(TAG, "Unknown URI: $uri")
                null
            }
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ctx = context ?: return null

        if (uriMatcher.match(uri) != STICKERS_ASSET_CODE) {
            Log.w(TAG, "openFile called with non-asset URI: $uri")
            return null
        }

        val pathSegments = uri.pathSegments
        if (pathSegments.size < 3) return null

        val packId = pathSegments[1]
        val fileName = pathSegments[2]
        val manager = StickerPackManager.getInstance(ctx)

        val file = manager.getStickerFile(packId, fileName)
        if (!file.exists()) {
            Log.w(TAG, "Sticker file not found: ${file.absolutePath}")
            return null
        }

        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun getPacksCursor(packs: List<StickerPack>): Cursor {
        val cursor = MatrixCursor(
            arrayOf(
                STICKER_PACK_IDENTIFIER,
                STICKER_PACK_NAME,
                STICKER_PACK_PUBLISHER,
                STICKER_PACK_ICON,
                ANDROID_APP_DOWNLOAD_LINK,
                IOS_APP_DOWNLOAD_LINK,
                PUBLISHER_EMAIL,
                PUBLISHER_WEBSITE,
                PRIVACY_POLICY_WEBSITE,
                LICENSE_AGREEMENT_WEBSITE,
                IMAGE_DATA_VERSION,
                AVOID_CACHE,
                ANIMATED_STICKER_PACK
            )
        )

        for (pack in packs) {
            cursor.addRow(
                arrayOf(
                    pack.identifier,
                    pack.name,
                    pack.publisher,
                    pack.trayImageFile,
                    "", // Android download link
                    "", // iOS download link
                    pack.publisherEmail,
                    pack.publisherWebsite,
                    pack.privacyPolicyWebsite,
                    pack.licenseAgreementWebsite,
                    pack.imageDataVersion,
                    if (pack.avoidCache) 1 else 0,
                    if (pack.animatedStickerPack) 1 else 0
                )
            )
        }

        cursor.setNotificationUri(context?.contentResolver, Uri.parse("content://$authority/metadata"))
        return cursor
    }

    private fun getStickersCursor(pack: StickerPack): Cursor {
        val cursor = MatrixCursor(
            arrayOf(STICKER_FILE_NAME, STICKER_FILE_EMOJI)
        )

        for (sticker in pack.stickers) {
            cursor.addRow(
                arrayOf(
                    sticker.imageFileName,
                    sticker.emojis.joinToString(",")
                )
            )
        }

        cursor.setNotificationUri(
            context?.contentResolver,
            Uri.parse("content://$authority/stickers/${pack.identifier}")
        )
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            METADATA_CODE -> "vnd.android.cursor.dir/vnd.$authority.metadata"
            METADATA_CODE_FOR_SINGLE_PACK -> "vnd.android.cursor.item/vnd.$authority.metadata"
            STICKERS_CODE -> "vnd.android.cursor.dir/vnd.$authority.stickers"
            STICKERS_ASSET_CODE -> "image/webp"
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
