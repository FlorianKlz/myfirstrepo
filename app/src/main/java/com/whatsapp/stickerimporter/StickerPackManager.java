package com.whatsapp.stickerimporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Manages sticker packs including scanning, creating, and organizing stickers
 */
public class StickerPackManager {
    private static final String TAG = "StickerPackManager";
    private static final String PREFS_NAME = "StickerPackPrefs";
    private static final String KEY_STICKER_PACKS = "sticker_packs";

    // WhatsApp sticker requirements
    private static final int MIN_STICKERS_PER_PACK = 3;
    private static final int MAX_STICKERS_PER_PACK = 30;
    private static final long MAX_FILE_SIZE = 100 * 1024; // 100 KB
    private static final int TRAY_IMAGE_SIZE = 96;

    private static StickerPackManager instance;
    private final Context context;
    private final List<StickerPack> stickerPacks;
    private final SharedPreferences preferences;

    private StickerPackManager(Context context) {
        this.context = context.getApplicationContext();
        this.stickerPacks = new ArrayList<>();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadStickerPacks();
    }

    public static synchronized StickerPackManager getInstance(Context context) {
        if (instance == null) {
            instance = new StickerPackManager(context);
        }
        return instance;
    }

    public List<StickerPack> getAllStickerPacks() {
        return new ArrayList<>(stickerPacks);
    }

    public StickerPack getStickerPackById(String identifier) {
        for (StickerPack pack : stickerPacks) {
            if (pack.identifier.equals(identifier)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Scans a directory for WebP files and creates sticker packs
     * @param sourceDirectory Directory containing WebP files
     * @return Number of sticker packs created
     */
    public int scanAndCreateStickerPacks(File sourceDirectory) {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            Log.e(TAG, "Invalid source directory: " + sourceDirectory);
            return 0;
        }

        // Find all WebP files
        List<File> webpFiles = findWebPFiles(sourceDirectory);
        Log.d(TAG, "Found " + webpFiles.size() + " WebP files");

        if (webpFiles.isEmpty()) {
            return 0;
        }

        // Organize files into packs of 30 stickers
        int packsCreated = 0;
        List<List<File>> chunks = chunkFiles(webpFiles, MAX_STICKERS_PER_PACK);

        for (int i = 0; i < chunks.size(); i++) {
            List<File> chunk = chunks.get(i);
            if (chunk.size() >= MIN_STICKERS_PER_PACK) {
                String packName = "Sticker Pack " + (i + 1);
                if (createStickerPack(packName, chunk, i)) {
                    packsCreated++;
                }
            }
        }

        saveStickerPacks();
        return packsCreated;
    }

    private List<File> findWebPFiles(File directory) {
        List<File> webpFiles = new ArrayList<>();
        findWebPFilesRecursive(directory, webpFiles);
        Collections.sort(webpFiles, (f1, f2) -> f1.getName().compareTo(f2.getName()));
        return webpFiles;
    }

    private void findWebPFilesRecursive(File directory, List<File> result) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findWebPFilesRecursive(file, result);
            } else if (file.getName().toLowerCase().endsWith(".webp")) {
                result.add(file);
            }
        }
    }

    private List<List<File>> chunkFiles(List<File> files, int chunkSize) {
        List<List<File>> chunks = new ArrayList<>();
        for (int i = 0; i < files.size(); i += chunkSize) {
            chunks.add(new ArrayList<>(files.subList(i, Math.min(files.size(), i + chunkSize))));
        }
        return chunks;
    }

    private boolean createStickerPack(String name, List<File> stickerFiles, int packIndex) {
        try {
            String identifier = "pack_" + packIndex + "_" + UUID.randomUUID().toString().substring(0, 8);

            StickerPack stickerPack = new StickerPack(
                identifier,
                name,
                "Sticker Importer",
                "tray_icon.webp",
                "",
                "",
                "",
                ""
            );

            // Create directory for this sticker pack
            File packDir = new File(context.getFilesDir(), "sticker_packs/" + identifier);
            if (!packDir.exists() && !packDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + packDir);
                return false;
            }

            // Copy stickers and validate
            List<Sticker> validStickers = new ArrayList<>();
            long totalSize = 0;

            for (int i = 0; i < stickerFiles.size(); i++) {
                File sourceFile = stickerFiles.get(i);
                String stickerFileName = "sticker_" + i + ".webp";
                File destFile = new File(packDir, stickerFileName);

                try {
                    // Validate and copy file
                    if (validateAndCopySticker(sourceFile, destFile)) {
                        Sticker sticker = new Sticker(stickerFileName, Arrays.asList("😀"));
                        sticker.size = destFile.length();
                        validStickers.add(sticker);
                        totalSize += sticker.size;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing sticker: " + sourceFile.getName(), e);
                }
            }

            if (validStickers.size() < MIN_STICKERS_PER_PACK) {
                Log.e(TAG, "Not enough valid stickers for pack: " + name);
                deleteDirectory(packDir);
                return false;
            }

            stickerPack.stickers = validStickers;
            stickerPack.totalSize = totalSize;

            // Create tray icon from first sticker
            createTrayIcon(packDir, validStickers.get(0).imageFileName);

            // Add tray icon size to total
            File trayFile = new File(packDir, "tray_icon.webp");
            if (trayFile.exists()) {
                stickerPack.totalSize += trayFile.length();
            }

            stickerPacks.add(stickerPack);
            Log.d(TAG, "Created sticker pack: " + name + " with " + validStickers.size() + " stickers");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error creating sticker pack: " + name, e);
            return false;
        }
    }

    private boolean validateAndCopySticker(File sourceFile, File destFile) throws IOException {
        // Check file size
        if (sourceFile.length() > MAX_FILE_SIZE) {
            Log.w(TAG, "File too large: " + sourceFile.getName() + " (" + sourceFile.length() + " bytes)");
            // Try to compress the image
            return compressWebP(sourceFile, destFile);
        }

        // Simple copy if size is OK
        copyFile(sourceFile, destFile);
        return true;
    }

    private boolean compressWebP(File sourceFile, File destFile) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap: " + sourceFile.getName());
                return false;
            }

            // Resize if needed to reduce file size
            int maxDimension = 512;
            if (bitmap.getWidth() > maxDimension || bitmap.getHeight() > maxDimension) {
                float scale = Math.min((float) maxDimension / bitmap.getWidth(),
                                       (float) maxDimension / bitmap.getHeight());
                int newWidth = Math.round(bitmap.getWidth() * scale);
                int newHeight = Math.round(bitmap.getHeight() * scale);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            // Save with compression
            FileOutputStream out = new FileOutputStream(destFile);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out);
            out.close();
            bitmap.recycle();

            return destFile.length() <= MAX_FILE_SIZE;

        } catch (Exception e) {
            Log.e(TAG, "Error compressing WebP: " + sourceFile.getName(), e);
            return false;
        }
    }

    private void createTrayIcon(File packDir, String firstStickerFileName) {
        try {
            File stickerFile = new File(packDir, firstStickerFileName);
            File trayFile = new File(packDir, "tray_icon.webp");

            Bitmap bitmap = BitmapFactory.decodeFile(stickerFile.getAbsolutePath());
            if (bitmap == null) {
                Log.e(TAG, "Failed to create tray icon from: " + firstStickerFileName);
                return;
            }

            // Create 96x96 tray icon
            Bitmap trayBitmap = Bitmap.createScaledBitmap(bitmap, TRAY_IMAGE_SIZE, TRAY_IMAGE_SIZE, true);

            FileOutputStream out = new FileOutputStream(trayFile);
            trayBitmap.compress(Bitmap.CompressFormat.WEBP, 100, out);
            out.close();

            bitmap.recycle();
            trayBitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "Error creating tray icon", e);
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    public void deleteStickerPack(String identifier) {
        StickerPack packToRemove = null;
        for (StickerPack pack : stickerPacks) {
            if (pack.identifier.equals(identifier)) {
                packToRemove = pack;
                break;
            }
        }

        if (packToRemove != null) {
            stickerPacks.remove(packToRemove);

            // Delete files
            File packDir = new File(context.getFilesDir(), "sticker_packs/" + identifier);
            deleteDirectory(packDir);

            saveStickerPacks();
        }
    }

    public void clearAllPacks() {
        stickerPacks.clear();

        // Delete all pack directories
        File stickerPacksDir = new File(context.getFilesDir(), "sticker_packs");
        deleteDirectory(stickerPacksDir);

        saveStickerPacks();
    }

    private void loadStickerPacks() {
        stickerPacks.clear();

        // Load from file system
        File stickerPacksDir = new File(context.getFilesDir(), "sticker_packs");
        if (!stickerPacksDir.exists()) {
            return;
        }

        File[] packDirs = stickerPacksDir.listFiles();
        if (packDirs == null) {
            return;
        }

        for (File packDir : packDirs) {
            if (packDir.isDirectory()) {
                try {
                    StickerPack pack = loadStickerPackFromDirectory(packDir);
                    if (pack != null) {
                        stickerPacks.add(pack);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading pack from: " + packDir.getName(), e);
                }
            }
        }

        Log.d(TAG, "Loaded " + stickerPacks.size() + " sticker packs");
    }

    private StickerPack loadStickerPackFromDirectory(File packDir) {
        String identifier = packDir.getName();

        // Count stickers
        File[] files = packDir.listFiles((dir, name) -> name.startsWith("sticker_") && name.endsWith(".webp"));
        if (files == null || files.length < MIN_STICKERS_PER_PACK) {
            return null;
        }

        // Extract pack index from identifier
        String[] parts = identifier.split("_");
        int packIndex = 0;
        if (parts.length > 1) {
            try {
                packIndex = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // Use default
            }
        }

        String packName = "Sticker Pack " + (packIndex + 1);

        StickerPack pack = new StickerPack(
            identifier,
            packName,
            "Sticker Importer",
            "tray_icon.webp",
            "",
            "",
            "",
            ""
        );

        // Load stickers
        List<Sticker> stickers = new ArrayList<>();
        Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        long totalSize = 0;
        for (File file : files) {
            Sticker sticker = new Sticker(file.getName(), Arrays.asList("😀"));
            sticker.size = file.length();
            stickers.add(sticker);
            totalSize += sticker.size;
        }

        pack.stickers = stickers;
        pack.totalSize = totalSize;

        // Add tray icon size
        File trayFile = new File(packDir, "tray_icon.webp");
        if (trayFile.exists()) {
            pack.totalSize += trayFile.length();
        }

        return pack;
    }

    private void saveStickerPacks() {
        Set<String> identifiers = new HashSet<>();
        for (StickerPack pack : stickerPacks) {
            identifiers.add(pack.identifier);
        }
        preferences.edit().putStringSet(KEY_STICKER_PACKS, identifiers).apply();
    }
}
