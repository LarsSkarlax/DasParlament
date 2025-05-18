package com.lrs.dasparlament

// Manager for the JSON File

import android.content.Context
import androidx.pdf.util.persistence.Json
import kotlinx.serialization.Serializable

@Serializable
data class YearData(
    val year: Int,
    val events: List<String>
)

@Serializable
data class YearList(
    val years: List<YearData>
)
