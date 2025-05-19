package com.lrs.dasparlament

import android.content.Context
import com.google.android.filament.View
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun readYearsFromAssets(context: Context): YearList {
    val jsonString = context.assets.open("ausgaben.json")
        .bufferedReader().use { it.readText() }

    val type = object : TypeToken<YearList>() {}.type
    return Gson().fromJson(jsonString, type)
}

fun getEventCountForYear(context: Context, targetYear: Int): Int {
    val yearList = readYearsFromAssets(context)
    return yearList.years.find { it.year == targetYear }?.events?.size ?: 0
}

fun getEventAtIndexForYear(context: Context, targetYear: Int, index: Int): String? {
    val yearList = readYearsFromAssets(context)
    val events = yearList.years.find { it.year == targetYear }?.events
    return if (events != null && index in events.indices) events[index] else null
}

fun getYearCount(context: Context): Int {
    val yearList = readYearsFromAssets(context)
    return yearList.years.size
}

fun getYearAtIndex(context: Context, index: Int): Int? {
    val yearList = readYearsFromAssets(context)
    return if (index in yearList.years.indices) {
        yearList.years[index].year
    } else {
        null
    }
}


fun getUrl(pdfIndex: String, year: String): String {
    val prefix = "https://www.das-parlament.de/epaper/"
    val pastfix = "/epaper/ausgabe.pdf"
    return "$prefix$year/$pdfIndex$pastfix"
}
