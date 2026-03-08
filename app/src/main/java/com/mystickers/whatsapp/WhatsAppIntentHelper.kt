package com.mystickers.whatsapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Helper for adding sticker packs to WhatsApp.
 */
object WhatsAppIntentHelper {

    private const val TAG = "WhatsAppIntentHelper"
    private const val WHATSAPP_PACKAGE = "com.whatsapp"
    private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    private const val ADD_PACK_ACTION = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
    private const val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    private const val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    private const val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"

    const val ADD_PACK_REQUEST_CODE = 200

    /**
     * Launches the WhatsApp intent to add a sticker pack.
     */
    fun addStickerPackToWhatsApp(
        activity: Activity,
        pack: StickerPack,
        useBusinessWhatsApp: Boolean = false
    ) {
        val intent = createAddStickerPackIntent(pack, useBusinessWhatsApp)
        try {
            activity.startActivityForResult(intent, ADD_PACK_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            val appName = if (useBusinessWhatsApp) "WhatsApp Business" else "WhatsApp"
            Toast.makeText(
                activity,
                "$appName ist nicht installiert",
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "WhatsApp not installed", e)
        }
    }

    /**
     * Creates the intent for adding a sticker pack to WhatsApp.
     */
    private fun createAddStickerPackIntent(
        pack: StickerPack,
        useBusinessWhatsApp: Boolean
    ): Intent {
        val targetPackage = if (useBusinessWhatsApp) WHATSAPP_BUSINESS_PACKAGE else WHATSAPP_PACKAGE
        return Intent().apply {
            action = ADD_PACK_ACTION
            putExtra(EXTRA_STICKER_PACK_ID, pack.identifier)
            putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
            putExtra(EXTRA_STICKER_PACK_NAME, pack.name)
            setPackage(targetPackage)
        }
    }

    /**
     * Checks if WhatsApp is installed on the device.
     */
    fun isWhatsAppInstalled(activity: Activity): Boolean {
        return try {
            activity.packageManager.getPackageInfo(WHATSAPP_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if WhatsApp Business is installed on the device.
     */
    fun isWhatsAppBusinessInstalled(activity: Activity): Boolean {
        return try {
            activity.packageManager.getPackageInfo(WHATSAPP_BUSINESS_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
