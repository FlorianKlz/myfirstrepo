package com.example.stickerimporter

import android.Manifest
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stickerimporter.data.StickerImporter
import com.example.stickerimporter.model.StickerPack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: StickerViewModel by viewModels {
        StickerViewModel.factory(application)
    }

    private val folderPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, flags)
                viewModel.onFolderSelected(uri)
            }
        }

    private val readPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                folderPicker.launch(null)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StickerImporterTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val snackbarHostState = rememberSnackbarHostState()
                    val state by viewModel.uiState.collectAsState()
                    val scope = rememberCoroutineScope()
                    ImporterScreen(
                        state = state,
                        onPickFolder = {
                            permissionRequest.launch(readPermission)
                        },
                        onPrepare = { viewModel.preparePacks() },
                        onImportPack = { pack ->
                            val success = addPackToWhatsApp(pack)
                            if (!success) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        getString(R.string.whatsapp_error)
                                    )
                                }
                            }
                        },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    private fun addPackToWhatsApp(pack: StickerPack): Boolean {
        val intent = Intent("com.whatsapp.intent.action.ENABLE_STICKER_PACK")
        intent.putExtra("sticker_pack_id", pack.identifier)
        intent.putExtra("sticker_pack_authority", "${packageName}.stickercontentprovider")
        intent.putExtra("sticker_pack_name", pack.name)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}

data class ImportUiState(
    val selectedFolder: Uri? = null,
    val stickerCount: Int = 0,
    val packs: List<StickerPack> = emptyList(),
    val isBusy: Boolean = false,
    val message: String? = null
)

class StickerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val importer = StickerImporter(application)
    private val _uiState = MutableStateFlow(
        ImportUiState(
            packs = importer.existingPacks()
        )
    )
    val uiState: StateFlow<ImportUiState> = _uiState

    fun onFolderSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedFolder = uri, isBusy = true, message = null)
        viewModelScope.launch {
            val count = importer.scanWebpCount(uri)
            _uiState.value = _uiState.value.copy(
                stickerCount = count,
                isBusy = false,
                message = if (count == 0) "Keine WebP Sticker gefunden" else null
            )
        }
    }

    fun preparePacks() {
        val folder = _uiState.value.selectedFolder ?: return
        _uiState.value = _uiState.value.copy(isBusy = true, message = null)
        viewModelScope.launch {
            val created = importer.prepareStickerPacks(folder, "Import")
            val all = importer.existingPacks()
            _uiState.value = _uiState.value.copy(
                packs = all,
                isBusy = false,
                message = if (created.isEmpty()) "Keine gültigen Sticker kopiert" else "${created.size} Pakete erzeugt"
            )
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(StickerViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return StickerViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}

@Composable
private fun ImporterScreen(
    state: ImportUiState,
    onPickFolder: () -> Unit,
    onPrepare: () -> Unit,
    onImportPack: (StickerPack) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) })
            Text(
                text = stringResource(id = R.string.instruction),
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = onPickFolder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.pick_folder))
            }
            if (state.selectedFolder != null) {
                Text(
                    text = "Gewählter Ordner: ${state.selectedFolder}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = "Gefundene Sticker: ${state.stickerCount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onPrepare,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedFolder != null && !state.isBusy
            ) {
                Text(text = stringResource(id = R.string.prepare_packs))
            }
            state.message?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
            if (state.isBusy) {
                Text(text = stringResource(id = R.string.processing))
            }
            if (state.packs.isNotEmpty()) {
                Text(
                    text = "${stringResource(id = R.string.packs_ready)} (${state.packs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                state.packs.forEach { pack ->
                    StickerPackCard(pack = pack, onImportPack = onImportPack)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun StickerPackCard(
    pack: StickerPack,
    onImportPack: (StickerPack) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = pack.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "Sticker: ${pack.stickers.size}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ID: ${pack.identifier.take(8)}…",
                    style = MaterialTheme.typography.labelSmall
                )
                Button(onClick = { onImportPack(pack) }) {
                    Text(stringResource(id = R.string.import_to_whatsapp))
                }
            }
        }
    }
}
