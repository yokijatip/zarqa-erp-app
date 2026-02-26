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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.snapshotFlow
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
    val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 2 })

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.clearError()
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (selectedTab != page) selectedTab = page
        }
    }

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) {
            pagerState.animateScrollToPage(selectedTab)
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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
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
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Total tersedia",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$totalTersedia pcs",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "${items.size} ukuran",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Rincian ukuran",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items.sortedBy { it.ukuran }.forEach { stok ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(999.dp)
                                        ) {
                                            Text(
                                                text = "${stok.ukuran} ${stok.stok_tersedia} pcs",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
    val totalInput = inputPerUkuran.values.sumOf { it.toIntOrNull() ?: 0 }
    val totalAvailable = selectedModelItems.sumOf { it.stok_tersedia }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card: Pilih Model
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pilih Model",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedModelName.ifEmpty { "" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Model") },
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

                if (selectedModelItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WarnaChip(
                            namaWarna = selectedModelItems.firstOrNull()?.nama_warna,
                            kodeHexWarna = selectedModelItems.firstOrNull()?.kode_hex_warna
                        )
                        Text(
                            text = "Stok total: $totalAvailable pcs",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedModelItems.forEach { stok ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "${stok.ukuran} ${stok.stok_tersedia} pcs",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card: Jumlah per Ukuran
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Jumlah per Ukuran",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (selectedModelItems.isEmpty()) {
                    Text(
                        text = "Pilih model terlebih dahulu",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    selectedModelItems.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { stok ->
                                OutlinedTextField(
                                    value = inputPerUkuran[stok.ukuran] ?: "",
                                    onValueChange = { value ->
                                        inputPerUkuran = inputPerUkuran + (stok.ukuran to value)
                                        formError = null
                                    },
                                    label = { Text("${stok.ukuran} (stok: ${stok.stok_tersedia})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    isError = formError != null
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Card: Tujuan & Keterangan
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tujuan & Keterangan",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tujuan,
                    onValueChange = { tujuan = it; formError = null },
                    label = { Text("Tujuan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = formError != null && tujuan.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (opsional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (formError != null) {
            Text(
                text = formError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Ringkasan singkat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total keluar: $totalInput pcs",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (selectedModelItems.isNotEmpty()) {
                Text(
                    text = "Sisa: ${(totalAvailable - totalInput).coerceAtLeast(0)} pcs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
