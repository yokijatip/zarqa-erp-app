package com.yoki.zarqaproduction.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yoki.zarqaproduction.data.model.BatchProduksi
import com.yoki.zarqaproduction.util.daysSince
import com.yoki.zarqaproduction.util.toFormattedDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BatchCard(
    batch: BatchProduksi,
    onPrimaryAction: () -> Unit,
    primaryLabel: String
) {
    val totalAwal = batch.total_pcs
    val pcsSaatIni = batch.pcs_saat_ini ?: batch.total_pcs
    val needsSync = batch.pcs_saat_ini == null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: nama model + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = batch.nama_model,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (needsSync) {
                        Text(
                            text = "Belum sinkron",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                StatusChip(status = batch.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Warna + tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WarnaChip(
                    namaWarna = batch.nama_warna,
                    kodeHexWarna = batch.kode_hex_warna
                )
                if (batch.createdAt != null) {
                    val daysSince = batch.createdAt.daysSince()
                    val dateStr = batch.createdAt.toFormattedDate()
                    val dayLabel = when {
                        daysSince == 0L -> "Hari ini"
                        daysSince == 1L -> "Kemarin"
                        else            -> "Hari ke-$daysSince"
                    }
                    Text(
                        text = "$dateStr · $dayLabel",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ringkasan angka
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total awal",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalAwal pcs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Saat ini",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$pcsSaatIni pcs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Varian",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${batch.detail_ukuran.size} ukuran",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Detail ukuran
            if (batch.detail_ukuran.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    batch.detail_ukuran.forEach { detail ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "${detail.ukuran} ${detail.jumlah_pcs} pcs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Aksi
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aksi",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onPrimaryAction
                ) {
                    Text(text = primaryLabel)
                }
            }
        }
    }
}
