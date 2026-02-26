package com.yoki.zarqaproduction.data.model

enum class StatusBatch(val label: String) {
    PENDING_CUTTING("Menunggu Cutting"),
    CUTTING_IN_PROGRESS("Sedang Cutting"),
    CUTTING_DONE("Cutting Selesai"),
    JAHIT_IN_PROGRESS("Sedang Jahit"),
    JAHIT_DONE("Jahit Selesai"),
    STEAM_IN_PROGRESS("Sedang Steam"),
    STEAM_DONE("Steam Selesai"),
    COMPLETED("Selesai");

    companion object {
        fun fromString(value: String): StatusBatch? =
            entries.find { it.name == value }
    }
}
