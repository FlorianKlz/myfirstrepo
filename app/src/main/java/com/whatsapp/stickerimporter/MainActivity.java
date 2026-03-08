package com.whatsapp.stickerimporter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

/**
 * Main Activity for the WhatsApp Sticker Importer
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private StickerPackAdapter adapter;
    private TextView statusText;
    private Button scanButton;
    private Button clearButton;
    private ProgressBar progressBar;

    private StickerPackManager stickerPackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stickerPackManager = StickerPackManager.getInstance(this);

        initViews();
        setupRecyclerView();
        checkWhatsAppInstallation();
        updateUI();

        // Check permissions
        if (checkPermissions()) {
            loadStickerPacks();
        } else {
            requestPermissions();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        statusText = findViewById(R.id.statusText);
        scanButton = findViewById(R.id.scanButton);
        clearButton = findViewById(R.id.clearButton);
        progressBar = findViewById(R.id.progressBar);

        scanButton.setOnClickListener(v -> onScanClicked());
        clearButton.setOnClickListener(v -> onClearClicked());
    }

    private void setupRecyclerView() {
        adapter = new StickerPackAdapter(this, stickerPackManager.getAllStickerPacks(),
            new StickerPackAdapter.OnStickerPackClickListener() {
                @Override
                public void onAddToWhatsApp(StickerPack stickerPack) {
                    addStickerPackToWhatsApp(stickerPack);
                }

                @Override
                public void onDelete(StickerPack stickerPack) {
                    deleteStickerPack(stickerPack);
                }

                @Override
                public void onViewDetails(StickerPack stickerPack) {
                    viewStickerPackDetails(stickerPack);
                }
            });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void checkWhatsAppInstallation() {
        if (!WhatsAppHelper.isWhatsAppInstalled(this)) {
            new AlertDialog.Builder(this)
                .setTitle("WhatsApp Not Installed")
                .setMessage("WhatsApp is not installed on this device. Please install WhatsApp to use sticker packs.")
                .setPositiveButton("Install", (dialog, which) -> {
                    // Open Play Store
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.whatsapp")));
                    } catch (Exception e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we need to request MANAGE_EXTERNAL_STORAGE
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadStickerPacks();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access sticker files.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermissions()) {
            loadStickerPacks();
        }
    }

    private void loadStickerPacks() {
        List<StickerPack> packs = stickerPackManager.getAllStickerPacks();
        adapter.updateStickerPacks(packs);
        updateUI();
    }

    private void updateUI() {
        List<StickerPack> packs = stickerPackManager.getAllStickerPacks();

        if (packs.isEmpty()) {
            statusText.setText("No sticker packs found.\n\nClick 'Scan for Stickers' to import WebP files from your device.");
            statusText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            clearButton.setEnabled(false);
        } else {
            int totalStickers = 0;
            for (StickerPack pack : packs) {
                totalStickers += pack.stickers.size();
            }
            statusText.setText(packs.size() + " sticker packs (" + totalStickers + " stickers) ready");
            statusText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            clearButton.setEnabled(true);
        }
    }

    private void onScanClicked() {
        if (!checkPermissions()) {
            requestPermissions();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Scan for Stickers")
            .setMessage("This will scan your device for WebP files and create sticker packs. " +
                       "Each pack will contain up to 30 stickers.\n\n" +
                       "Where are your sticker files located?")
            .setItems(new CharSequence[]{"Pictures folder", "Downloads folder", "Custom folder"}, (dialog, which) -> {
                File scanDirectory;
                switch (which) {
                    case 0:
                        scanDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        break;
                    case 1:
                        scanDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        break;
                    case 2:
                        // For simplicity, scan entire external storage
                        scanDirectory = Environment.getExternalStorageDirectory();
                        break;
                    default:
                        return;
                }
                performScan(scanDirectory);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performScan(File directory) {
        progressBar.setVisibility(View.VISIBLE);
        scanButton.setEnabled(false);

        // Run scan in background thread
        new Thread(() -> {
            try {
                final int packsCreated = stickerPackManager.scanAndCreateStickerPacks(directory);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    scanButton.setEnabled(true);

                    if (packsCreated > 0) {
                        Toast.makeText(MainActivity.this,
                            "Created " + packsCreated + " sticker packs", Toast.LENGTH_LONG).show();
                        loadStickerPacks();
                    } else {
                        Toast.makeText(MainActivity.this,
                            "No valid WebP files found in " + directory.getName(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error scanning for stickers", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    scanButton.setEnabled(true);
                    Toast.makeText(MainActivity.this,
                        "Error scanning for stickers: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void onClearClicked() {
        new AlertDialog.Builder(this)
            .setTitle("Clear All Packs")
            .setMessage("Are you sure you want to delete all sticker packs? This cannot be undone.")
            .setPositiveButton("Delete All", (dialog, which) -> {
                stickerPackManager.clearAllPacks();
                loadStickerPacks();
                Toast.makeText(this, "All sticker packs deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void addStickerPackToWhatsApp(StickerPack stickerPack) {
        try {
            WhatsAppHelper.addStickerPackToWhatsApp(this, stickerPack.identifier, stickerPack.name);
        } catch (Exception e) {
            Log.e(TAG, "Error adding sticker pack to WhatsApp", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStickerPack(StickerPack stickerPack) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Sticker Pack")
            .setMessage("Delete \"" + stickerPack.name + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                stickerPackManager.deleteStickerPack(stickerPack.identifier);
                loadStickerPacks();
                Toast.makeText(this, "Sticker pack deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void viewStickerPackDetails(StickerPack stickerPack) {
        Intent intent = new Intent(this, StickerPackDetailsActivity.class);
        intent.putExtra("sticker_pack", stickerPack);
        startActivity(intent);
    }
}
