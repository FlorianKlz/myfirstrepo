package com.mystickers.whatsapp

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.io.File

/**
 * Main activity for the WhatsApp Sticker Importer app.
 * Allows the user to:
 * - Select a directory containing .webp sticker files
 * - Automatically organize them into WhatsApp-compatible packs of 30
 * - Add individual packs or all packs to WhatsApp
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSelectFolder: Button
    private lateinit var btnAddAllToWhatsApp: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvStickerCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: View

    private val stickerPacks = mutableListOf<StickerPack>()
    private lateinit var adapter: StickerPackAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val folderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFolder(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        checkPermissions()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        btnSelectFolder = findViewById(R.id.btn_select_folder)
        btnAddAllToWhatsApp = findViewById(R.id.btn_add_all)
        tvStatus = findViewById(R.id.tv_status)
        tvStickerCount = findViewById(R.id.tv_sticker_count)
        progressBar = findViewById(R.id.progress_bar)
        emptyView = findViewById(R.id.empty_view)

        btnSelectFolder.setOnClickListener { openFolderPicker() }
        btnAddAllToWhatsApp.setOnClickListener { addAllPacksToWhatsApp() }
        btnAddAllToWhatsApp.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        adapter = StickerPackAdapter(stickerPacks) { pack ->
            WhatsAppIntentHelper.addStickerPackToWhatsApp(this, pack)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ needs READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12: use MANAGE_EXTERNAL_STORAGE or scoped storage
            if (!Environment.isExternalStorageManager()) {
                showManageStoragePermissionDialog()
            }
        } else {
            // Android 10 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showManageStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Speicherzugriff erforderlich")
            .setMessage(
                "Diese App benötigt Zugriff auf den Gerätespeicher, " +
                "um Sticker-Dateien zu finden und zu importieren."
            )
            .setPositiveButton("Einstellungen öffnen") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Berechtigung erteilt", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Speicherberechtigung wird benötigt um Sticker zu laden",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        folderPickerLauncher.launch(intent)
    }

    private fun handleSelectedFolder(uri: Uri) {
        // Take persistent permission
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, flags)

        // Convert content URI to file path
        val docId = android.provider.DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        val path = if (split.size > 1) {
            val storageType = split[0]
            val relativePath = split[1]
            if ("primary".equals(storageType, ignoreCase = true)) {
                "${Environment.getExternalStorageDirectory().absolutePath}/$relativePath"
            } else {
                "/storage/$storageType/$relativePath"
            }
        } else {
            docId
        }

        val folder = File(path)
        if (!folder.exists() || !folder.isDirectory) {
            Toast.makeText(this, "Ordner nicht gefunden: $path", Toast.LENGTH_LONG).show()
            return
        }

        loadStickersFromFolder(folder)
    }

    private fun loadStickersFromFolder(folder: File) {
        tvStatus.text = "Sticker werden geladen..."
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        btnSelectFolder.isEnabled = false
        btnAddAllToWhatsApp.visibility = View.GONE

        coroutineScope.launch {
            val manager = StickerPackManager.getInstance(this@MainActivity)

            val packCount = withContext(Dispatchers.IO) {
                manager.loadStickersFromDirectory(
                    sourceDir = folder,
                    publisher = "My Stickers"
                ) { current, total ->
                    launch(Dispatchers.Main) {
                        tvStatus.text = "Verarbeite Sticker $current / $total..."
                        progressBar.progress = (current * 100 / total)
                    }
                }
            }

            // Update UI
            progressBar.visibility = View.GONE
            btnSelectFolder.isEnabled = true

            stickerPacks.clear()
            stickerPacks.addAll(manager.getStickerPacks())
            adapter.notifyDataSetChanged()

            if (packCount > 0) {
                val totalStickers = stickerPacks.sumOf { it.stickers.size }
                tvStatus.text = "Fertig! $totalStickers Sticker in $packCount Paketen"
                tvStickerCount.text = "$totalStickers Sticker gefunden"
                tvStickerCount.visibility = View.VISIBLE
                btnAddAllToWhatsApp.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            } else {
                tvStatus.text = "Keine WebP-Sticker im ausgewählten Ordner gefunden"
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun addAllPacksToWhatsApp() {
        if (stickerPacks.isEmpty()) {
            Toast.makeText(this, "Keine Sticker-Pakete vorhanden", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Alle Pakete hinzufügen")
            .setMessage(
                "Es werden ${stickerPacks.size} Sticker-Pakete nacheinander zu WhatsApp hinzugefügt. " +
                "Du musst jedes Paket einzeln in WhatsApp bestätigen."
            )
            .setPositiveButton("Starten") { _, _ ->
                addPackSequentially(0)
            }
            .setNegativeButton("Abbrechen", null)
            .show()
    }

    private var currentPackIndex = 0

    private fun addPackSequentially(index: Int) {
        if (index >= stickerPacks.size) {
            Toast.makeText(
                this,
                "Alle ${stickerPacks.size} Pakete wurden an WhatsApp gesendet!",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        currentPackIndex = index
        tvStatus.text = "Füge Paket ${index + 1} von ${stickerPacks.size} hinzu..."
        WhatsAppIntentHelper.addStickerPackToWhatsApp(this, stickerPacks[index])
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WhatsAppIntentHelper.ADD_PACK_REQUEST_CODE) {
            // Move to next pack after returning from WhatsApp
            if (currentPackIndex < stickerPacks.size - 1) {
                // Small delay to avoid overwhelming WhatsApp
                recyclerView.postDelayed({
                    addPackSequentially(currentPackIndex + 1)
                }, 500)
            } else {
                tvStatus.text = "Alle Pakete wurden verarbeitet!"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
