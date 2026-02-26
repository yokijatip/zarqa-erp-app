package com.yoki.zarqaproduction.ui.worker.jahit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.yoki.zarqaproduction.data.model.BatchProduksi
import com.yoki.zarqaproduction.data.model.UserProfile
import com.yoki.zarqaproduction.ui.common.BatchCard
import com.yoki.zarqaproduction.ui.worker.WorkerActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JahitScreen(
    userProfile: UserProfile,
    viewModel: JahitViewModel = viewModel()
) {
    val batches by viewModel.batches.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var batchToStart by remember { mutableStateOf<BatchProduksi?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var batchToFinish by remember { mutableStateOf<BatchProduksi?>(null) }
    var pcsBerhasil by rememberSaveable { mutableStateOf("") }
    var pcsReject by rememberSaveable { mutableStateOf("0") }
    var catatan by rememberSaveable { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userProfile.uid) {
        viewModel.initialize(userProfile.uid)
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Jahit")
                        Text(
                            text = userProfile.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = {
                        Firebase.auth.signOut()
                        (context as? WorkerActivity)?.finish()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = viewModel::loadBatches,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isLoading && batches.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada batch untuk dijahit",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(batches, key = { it.id }) { batch ->
                        val isAntri = batch.status == "CUTTING_DONE"
                        BatchCard(
                            batch = batch,
                            primaryLabel = if (isAntri) "Mulai Jahit" else "Selesai & Input",
                            onPrimaryAction = {
                                if (isAntri) {
                                    batchToStart = batch
                                } else {
                                    pcsBerhasil = ""; pcsReject = "0"; catatan = ""; inputError = null
                                    batchToFinish = batch
                                    scope.launch { bottomSheetState.show() }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    batchToStart?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchToStart = null },
            title = { Text("Mulai Jahit") },
            text = { Text("Mulai proses jahit untuk ${batch.nama_model}?") },
            confirmButton = {
                TextButton(onClick = {
                    batchToStart = null
                    viewModel.startJahit(batch.id, userProfile.uid, userProfile.name) { success ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (success) "Jahit dimulai!" else "Gagal memulai. Coba lagi."
                            )
                        }
                    }
                }) { Text("Mulai") }
            },
            dismissButton = {
                TextButton(onClick = { batchToStart = null }) { Text("Batal") }
            }
        )
    }

    if (batchToFinish != null) {
        ModalBottomSheet(
            onDismissRequest = { batchToFinish = null },
            sheetState = bottomSheetState,
            modifier = Modifier.imePadding()
        ) {
            val batch = batchToFinish!!
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Laporan Hasil Jahit", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${batch.nama_model} · ${batch.total_pcs} pcs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pcsBerhasil, onValueChange = { pcsBerhasil = it; inputError = null },
                        label = { Text("PCS Berhasil") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = inputError != null, singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pcsReject, onValueChange = { pcsReject = it },
                        label = { Text("PCS Reject") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                val berhasil = pcsBerhasil.toIntOrNull() ?: 0
                val reject = pcsReject.toIntOrNull() ?: 0
                val totalInput = berhasil + reject
                val melebihi = totalInput > batch.total_pcs
                Text("Total dilaporkan: $totalInput / ${batch.total_pcs} pcs",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (melebihi) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp))
                if (inputError != null) {
                    Text(inputError!!, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = catatan, onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") }, maxLines = 3, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { scope.launch { bottomSheetState.hide() }; batchToFinish = null }) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(onClick = {
                        val pcs    = pcsBerhasil.toIntOrNull()
                        val reject = pcsReject.toIntOrNull() ?: 0
                        when {
                            pcs == null || pcs <= 0 ->
                                inputError = "PCS Berhasil harus lebih dari 0"
                            reject < 0 ->
                                inputError = "PCS Reject tidak boleh negatif"
                            pcs + reject > batch.total_pcs ->
                                inputError = "Total (${pcs + reject} pcs) melebihi order ${batch.total_pcs} pcs"
                            else -> {
                                val batchId = batch.id
                                scope.launch { bottomSheetState.hide() }
                                batchToFinish = null
                                viewModel.finishJahit(batchId, userProfile.uid, userProfile.name, pcs, reject, catatan.ifBlank { null }) { success ->
                                    scope.launch { snackbarHostState.showSnackbar(if (success) "Jahit selesai!" else "Gagal menyimpan. Coba lagi.") }
                                }
                            }
                        }
                    }) { Text("Simpan & Selesai") }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
