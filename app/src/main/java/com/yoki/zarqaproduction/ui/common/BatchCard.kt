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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Baris atas: nama model + warna chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = batch.nama_model,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                WarnaChip(
                    namaWarna = batch.nama_warna,
                    kodeHexWarna = batch.kode_hex_warna
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Detail ukuran
            if (batch.detail_ukuran.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    batch.detail_ukuran.forEach { detail ->
                        Text(
                            text = "${detail.ukuran}:${detail.jumlah_pcs}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }

            Text(
                text = "Total: ${batch.total_pcs} pcs",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tanggal + hari ke berapa
            if (batch.createdAt != null) {
                val daysSince = batch.createdAt.daysSince()
                val dateStr = batch.createdAt.toFormattedDate()
                val dayLabel = when {
                    daysSince == 0L -> "Hari ini"
                    daysSince == 1L -> "Kemarin"
                    else            -> "Hari ke-$daysSince"
                }
                Text(
                    text = "Dibuat: $dateStr  ·  $dayLabel",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Status + tombol aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = batch.status)
                Button(onClick = onPrimaryAction) {
                    Text(text = primaryLabel)
                }
            }
        }
    }
}
