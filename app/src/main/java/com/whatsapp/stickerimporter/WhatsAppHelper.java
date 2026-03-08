package com.whatsapp.stickerimporter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

/**
 * Helper class for WhatsApp integration
 */
public class WhatsAppHelper {
    private static final String TAG = "WhatsAppHelper";

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";
    private static final String WHATSAPP_CONSUMER_PACKAGE = "com.whatsapp";

    /**
     * Check if WhatsApp is installed
     */
    public static boolean isWhatsAppInstalled(Context context) {
        return isPackageInstalled(context, WHATSAPP_PACKAGE) ||
               isPackageInstalled(context, WHATSAPP_CONSUMER_PACKAGE);
    }

    /**
     * Check if WhatsApp Business is installed
     */
    public static boolean isWhatsAppBusinessInstalled(Context context) {
        return isPackageInstalled(context, WHATSAPP_BUSINESS_PACKAGE);
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Add a sticker pack to WhatsApp
     */
    public static void addStickerPackToWhatsApp(Context context, String identifier, String stickerPackName) {
        Intent intent = createIntentToAddStickerPack(context, identifier, stickerPackName);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "WhatsApp not found", e);
            throw new IllegalStateException("WhatsApp is not installed", e);
        }
    }

    /**
     * Create intent to add sticker pack to WhatsApp
     */
    public static Intent createIntentToAddStickerPack(Context context, String identifier, String stickerPackName) {
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        intent.putExtra("sticker_pack_id", identifier);
        intent.putExtra("sticker_pack_authority", BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra("sticker_pack_name", stickerPackName);
        return intent;
    }

    /**
     * Get all sticker packs intent
     */
    public static Intent createIntentToViewAllStickerPacks(Context context) {
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        intent.putExtra("sticker_pack_authority", BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        return intent;
    }

    /**
     * Check if sticker pack is added to WhatsApp
     */
    public static boolean isStickerPackInstalled(Context context, String identifier) {
        try {
            // Query WhatsApp to check if pack is installed
            // This is a simplified check - actual implementation would query WhatsApp's content provider
            return false; // Conservative approach
        } catch (Exception e) {
            Log.e(TAG, "Error checking sticker pack status", e);
            return false;
        }
    }
}
