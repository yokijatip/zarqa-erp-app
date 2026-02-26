package com.yoki.zarqaproduction.ui.worker.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.yoki.zarqaproduction.data.model.BarangKeluar
import com.yoki.zarqaproduction.data.model.DetailKeluar
import com.yoki.zarqaproduction.data.model.StokBarangJadi
import com.yoki.zarqaproduction.data.model.UserProfile
import com.yoki.zarqaproduction.ui.common.EmptyScreen
import com.yoki.zarqaproduction.ui.common.WarnaChip
import com.yoki.zarqaproduction.ui.worker.WorkerActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userProfile: UserProfile,
    viewModel: AdminViewModel = viewModel()
) {
    val stokList by viewModel.stokList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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
                        Text("Admin Gudang")
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
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Stok Barang Jadi") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Catat Keluar") }
                )
            }

            when (selectedTab) {
                0 -> StokTab(
                    stokList = stokList,
                    isLoading = isLoading,
                    onRefresh = viewModel::loadStok
                )
                1 -> CatatKeluarTab(
                    stokList = stokList,
                    isSubmitting = isSubmitting,
                    currentUid = userProfile.uid,
                    onSubmit = { barangKeluar, stokIdPerUkuran ->
                        viewModel.catatKeluar(barangKeluar, stokIdPerUkuran) { success ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (success) "Barang keluar berhasil dicatat!"
                                    else "Gagal menyimpan. Coba lagi."
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

// ─── Tab 1: Stok Barang Jadi ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun StokTab(
    stokList: List<StokBarangJadi>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    // Group by model_id
    val grouped = stokList.groupBy { it.model_id }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isLoading && stokList.isEmpty()) {
            EmptyScreen("Stok barang jadi kosong")
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (_, items) ->
                    val first = items.first()
                    val totalTersedia = items.sumOf { it.stok_tersedia }

                    item(key = first.model_id) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = first.nama_model,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    WarnaChip(first.nama_warna, first.kode_hex_warna)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Total tersedia: $totalTersedia pcs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items.sortedBy { it.ukuran }.forEach { stok ->
                                        Text(
                                            text = "${stok.ukuran}: ${stok.stok_tersedia}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Tab 2: Catat Barang Keluar ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatatKeluarTab(
    stokList: List<StokBarangJadi>,
    isSubmitting: Boolean,
    currentUid: String,
    onSubmit: (BarangKeluar, Map<String, Int>) -> Unit
) {
    // Hanya model yang masih ada stok
    val modelOptions = stokList
        .filter { it.stok_tersedia > 0 }
        .groupBy { it.model_id }
        .keys
        .toList()

    var selectedModelId by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var tujuan by rememberSaveable { mutableStateOf("") }
    var keterangan by rememberSaveable { mutableStateOf("") }
    // Map ukuran → jumlah yang diinput
    var inputPerUkuran by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var formError by remember { mutableStateOf<String?>(null) }

    val selectedModelItems = stokList.filter {
        it.model_id == selectedModelId && it.stok_tersedia > 0
    }.sortedBy { it.ukuran }

    val selectedModelName = selectedModelItems.firstOrNull()?.nama_model ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pilih Model
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedModelName.ifEmpty { "" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Pilih Model") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                isError = formError != null && selectedModelId.isEmpty()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                modelOptions.forEach { modelId ->
                    val namaModel = stokList.first { it.model_id == modelId }.nama_model
                    DropdownMenuItem(
                        text = { Text(namaModel) },
                        onClick = {
                            selectedModelId = modelId
                            inputPerUkuran = emptyMap()
                            formError = null
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Input per ukuran (setelah model dipilih)
        if (selectedModelItems.isNotEmpty()) {
            Text(
                text = "Jumlah per Ukuran",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            selectedModelItems.forEach { stok ->
                OutlinedTextField(
                    value = inputPerUkuran[stok.ukuran] ?: "",
                    onValueChange = { value ->
                        inputPerUkuran = inputPerUkuran + (stok.ukuran to value)
                        formError = null
                    },
                    label = { Text("${stok.ukuran} (stok: ${stok.stok_tersedia})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = formError != null
                )
            }
        }

        // Tujuan
        OutlinedTextField(
            value = tujuan,
            onValueChange = { tujuan = it; formError = null },
            label = { Text("Tujuan") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = formError != null && tujuan.isBlank()
        )

        // Keterangan
        OutlinedTextField(
            value = keterangan,
            onValueChange = { keterangan = it },
            label = { Text("Keterangan (opsional)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (formError != null) {
            Text(
                text = formError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                // Validasi
                if (selectedModelId.isEmpty()) { formError = "Pilih model terlebih dahulu"; return@Button }
                if (tujuan.isBlank()) { formError = "Tujuan tidak boleh kosong"; return@Button }

                val detailList = selectedModelItems.mapNotNull { stok ->
                    val jumlah = inputPerUkuran[stok.ukuran]?.toIntOrNull() ?: 0
                    if (jumlah > 0) DetailKeluar(stok.ukuran, jumlah) else null
                }
                if (detailList.isEmpty()) { formError = "Masukkan jumlah minimal untuk satu ukuran"; return@Button }

                val melebihi = detailList.find { detail ->
                    val stok = selectedModelItems.find { it.ukuran == detail.ukuran }
                    (stok?.stok_tersedia ?: 0) < detail.jumlah_pcs
                }
                if (melebihi != null) {
                    formError = "Jumlah ukuran ${melebihi.ukuran} melebihi stok tersedia"
                    return@Button
                }

                val stokIdPerUkuran = detailList.associate { detail ->
                    val stok = selectedModelItems.first { it.ukuran == detail.ukuran }
                    stok.id to detail.jumlah_pcs
                }

                val barangKeluar = BarangKeluar(
                    model_id      = selectedModelId,
                    nama_model    = selectedModelName,
                    detail_keluar = detailList,
                    total_pcs     = detailList.sumOf { it.jumlah_pcs },
                    tujuan        = tujuan.trim(),
                    keterangan    = keterangan.ifBlank { null },
                    dicatat_oleh  = currentUid
                )

                onSubmit(barangKeluar, stokIdPerUkuran)

                // Reset form
                selectedModelId = ""
                inputPerUkuran = emptyMap()
                tujuan = ""
                keterangan = ""
                formError = null
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(4.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Catat Keluar")
            }
        }
    }
}
