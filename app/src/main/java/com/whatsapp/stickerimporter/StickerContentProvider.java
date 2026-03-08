package com.whatsapp.stickerimporter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Content Provider for exposing sticker packs to WhatsApp
 */
public class StickerContentProvider extends ContentProvider {
    private static final String TAG = "StickerContentProvider";

    // Authority
    public static String AUTHORITY;

    // URI matching codes
    private static final int METADATA = 1;
    private static final int METADATA_CODE = 2;
    private static final int STICKERS = 3;
    private static final int STICKERS_ASSET = 4;

    private static UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY;

        if (uriMatcher == null) {
            uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            uriMatcher.addURI(AUTHORITY, "metadata", METADATA);
            uriMatcher.addURI(AUTHORITY, "metadata/*", METADATA_CODE);
            uriMatcher.addURI(AUTHORITY, "stickers/*", STICKERS);
            uriMatcher.addURI(AUTHORITY, "stickers_asset/*/*", STICKERS_ASSET);
        }

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final int matchCode = uriMatcher.match(uri);

        switch (matchCode) {
            case METADATA:
                return getPackForAllStickerPacks(uri);
            case METADATA_CODE:
                return getCursorForSingleStickerPack(uri);
            case STICKERS:
                return getStickersForAStickerPack(uri);
            case STICKERS_ASSET:
                return getStickerAssetUri(uri);
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        final int matchCode = uriMatcher.match(uri);

        if (matchCode == STICKERS_ASSET) {
            return getImageAsset(uri);
        }
        return null;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        final int matchCode = uriMatcher.match(uri);

        if (matchCode == STICKERS_ASSET) {
            AssetFileDescriptor assetFileDescriptor = getImageAsset(uri);
            if (assetFileDescriptor != null) {
                return assetFileDescriptor.getParcelFileDescriptor();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int matchCode = uriMatcher.match(uri);

        switch (matchCode) {
            case METADATA:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".metadata";
            case METADATA_CODE:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".metadata";
            case STICKERS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".stickers";
            case STICKERS_ASSET:
                return "image/webp";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported");
    }

    private Cursor getPackForAllStickerPacks(@NonNull Uri uri) {
        return getStickerPackInfo(uri, StickerPackManager.getInstance(getContext()).getAllStickerPacks());
    }

    private Cursor getCursorForSingleStickerPack(@NonNull Uri uri) {
        String identifier = uri.getLastPathSegment();
        List<StickerPack> stickerPacks = new ArrayList<>();

        for (StickerPack stickerPack : StickerPackManager.getInstance(getContext()).getAllStickerPacks()) {
            if (identifier.equals(stickerPack.identifier)) {
                stickerPacks.add(stickerPack);
                break;
            }
        }

        return getStickerPackInfo(uri, stickerPacks);
    }

    private Cursor getStickerPackInfo(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                "sticker_pack_identifier",
                "sticker_pack_name",
                "sticker_pack_publisher",
                "sticker_pack_icon_image_file",
                "android_play_store_link",
                "ios_app_download_link",
                "publisher_email",
                "publisher_website",
                "privacy_policy_website",
                "license_agreement_website",
                "image_data_version",
                "avoid_cache"
        });

        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.identifier);
            builder.add(stickerPack.name);
            builder.add(stickerPack.publisher);
            builder.add(stickerPack.trayImageFile);
            builder.add(""); // Android play store link
            builder.add(""); // iOS app download link
            builder.add(stickerPack.publisherEmail != null ? stickerPack.publisherEmail : "");
            builder.add(stickerPack.publisherWebsite != null ? stickerPack.publisherWebsite : "");
            builder.add(stickerPack.privacyPolicyWebsite != null ? stickerPack.privacyPolicyWebsite : "");
            builder.add(stickerPack.licenseAgreementWebsite != null ? stickerPack.licenseAgreementWebsite : "");
            builder.add(stickerPack.imageDataVersion != null ? stickerPack.imageDataVersion : "1");
            builder.add(stickerPack.avoidCache ? 1 : 0);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getStickersForAStickerPack(@NonNull Uri uri) {
        String identifier = uri.getLastPathSegment();

        MatrixCursor cursor = new MatrixCursor(new String[]{"sticker_file_name", "sticker_emoji"});

        for (StickerPack stickerPack : StickerPackManager.getInstance(getContext()).getAllStickerPacks()) {
            if (identifier.equals(stickerPack.identifier)) {
                for (Sticker sticker : stickerPack.stickers) {
                    cursor.addRow(new Object[]{sticker.imageFileName, TextUtils.join(",", sticker.emojis)});
                }
                break;
            }
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getStickerAssetUri(@NonNull Uri uri) {
        // Not used in current implementation
        MatrixCursor cursor = new MatrixCursor(new String[]{"uri"});
        cursor.addRow(new Object[]{uri});
        return cursor;
    }

    private AssetFileDescriptor getImageAsset(@NonNull Uri uri) throws FileNotFoundException {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 3) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        String identifier = pathSegments.get(pathSegments.size() - 2);
        String fileName = pathSegments.get(pathSegments.size() - 1);

        Context context = getContext();
        if (context == null) {
            throw new IllegalStateException("Context is null");
        }

        // Get the file from internal storage
        File stickerPackDir = new File(context.getFilesDir(), "sticker_packs/" + identifier);
        File imageFile = new File(stickerPackDir, fileName);

        if (!imageFile.exists()) {
            Log.e(TAG, "File not found: " + imageFile.getAbsolutePath());
            throw new FileNotFoundException("Sticker image not found: " + fileName);
        }

        try {
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(imageFile, ParcelFileDescriptor.MODE_READ_ONLY);
            return new AssetFileDescriptor(parcelFileDescriptor, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
        } catch (IOException e) {
            Log.e(TAG, "Error opening file: " + imageFile.getAbsolutePath(), e);
            throw new FileNotFoundException("Error opening sticker file");
        }
    }
}
