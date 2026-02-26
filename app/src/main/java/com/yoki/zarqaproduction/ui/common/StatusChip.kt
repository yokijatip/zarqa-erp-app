package com.yoki.zarqaproduction.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yoki.zarqaproduction.util.statusBgColor
import com.yoki.zarqaproduction.util.statusLabel
import com.yoki.zarqaproduction.util.statusTextColor

@Composable
fun StatusChip(status: String) {
    Text(
        text = statusLabel(status),
        color = statusTextColor(status),
        fontSize = 12.sp,
        modifier = Modifier
            .background(statusBgColor(status), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
