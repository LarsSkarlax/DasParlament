package com.lrs.dasparlament

import android.content.Context
import android.util.Log
import com.google.android.filament.View
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.google.gson.annotations.SerializedName
import java.io.File


data class Ausgabe(
    val title: String,
    @SerializedName("date_published") val datePublished: String,
    @SerializedName("year_published") val yearPublished: Int,
    @SerializedName("cover_image") val coverImage: String,
    @SerializedName("ausgabe_number") val ausgabeNumber: String
)



fun readAusgabenFromAssets(context: Context): List<Ausgabe> {
    val file = File(context.filesDir, "ausgaben.json")
    val jsonString = if (file.exists()) {
        file.readText()
    } else {
        Log.d("LOADING", "Had to use ASSETS")
        Log.d("LOADING", "Had to use ASSETS")
        Log.d("LOADING", "Had to use ASSETS")
        context.assets.open("ausgaben.json").bufferedReader().use { it.readText() }
    }

    val gson = Gson()
    val jsonElement = JsonParser.parseString(jsonString)

    val ausgabeListType = object : TypeToken<List<Ausgabe>>() {}.type

    return when {
        // Fall 1: die Datei enthält direkt ein Array: [ { ... }, { ... } ]
        jsonElement.isJsonArray -> {
            gson.fromJson(jsonElement, ausgabeListType)
        }
        // Fall 2: die Datei enthält ein Objekt mit einem Feld "ausgaben": { "ausgaben": [ ... ] }
        jsonElement.isJsonObject && jsonElement.asJsonObject.has("ausgaben") -> {
            val arr = jsonElement.asJsonObject.getAsJsonArray("ausgaben")
            gson.fromJson(arr, ausgabeListType)
        }
        // Fall 3: die Datei enthält nur ein einzelnes Ausgabe-Objekt: { "title": "...", ... }
        jsonElement.isJsonObject -> {
            // Wir packen es in eine Liste mit genau einem Element
            listOf(gson.fromJson(jsonElement, Ausgabe::class.java))
        }
        else -> emptyList()
    }
}

fun getUrl(ausgabeNumber: String, year: Int): String {
    return "https://www.das-parlament.de/epaper/$year/$ausgabeNumber/epaper/ausgabe.pdf"
}
