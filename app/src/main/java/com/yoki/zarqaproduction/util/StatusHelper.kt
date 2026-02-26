package com.yoki.zarqaproduction.util

import androidx.compose.ui.graphics.Color

fun statusBgColor(status: String): Color = when (status) {
    "PENDING_CUTTING"     -> Color(0xFFE2E8F0)
    "CUTTING_IN_PROGRESS" -> Color(0xFFFED7AA)
    "CUTTING_DONE"        -> Color(0xFFFEF08A)
    "JAHIT_IN_PROGRESS"   -> Color(0xFFBFDBFE)
    "JAHIT_DONE"          -> Color(0xFF99F6E4)
    "STEAM_IN_PROGRESS"   -> Color(0xFFE9D5FF)
    "STEAM_DONE"          -> Color(0xFFA7F3D0)
    "COMPLETED"           -> Color(0xFFBBF7D0)
    else                  -> Color(0xFFF3F4F6)
}

fun statusTextColor(status: String): Color = when (status) {
    "PENDING_CUTTING"     -> Color(0xFF475569)
    "CUTTING_IN_PROGRESS" -> Color(0xFFC2410C)
    "CUTTING_DONE"        -> Color(0xFF854D0E)
    "JAHIT_IN_PROGRESS"   -> Color(0xFF1D4ED8)
    "JAHIT_DONE"          -> Color(0xFF0F766E)
    "STEAM_IN_PROGRESS"   -> Color(0xFF7E22CE)
    "STEAM_DONE"          -> Color(0xFF065F46)
    "COMPLETED"           -> Color(0xFF14532D)
    else                  -> Color(0xFF6B7280)
}

fun statusLabel(status: String): String = when (status) {
    "PENDING_CUTTING"     -> "Menunggu Cutting"
    "CUTTING_IN_PROGRESS" -> "Sedang Cutting"
    "CUTTING_DONE"        -> "Cutting Selesai"
    "JAHIT_IN_PROGRESS"   -> "Sedang Jahit"
    "JAHIT_DONE"          -> "Jahit Selesai"
    "STEAM_IN_PROGRESS"   -> "Sedang Steam"
    "STEAM_DONE"          -> "Steam Selesai"
    "COMPLETED"           -> "Selesai"
    else                  -> status
}
