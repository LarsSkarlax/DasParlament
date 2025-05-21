package com.lrs.dasparlament

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale

fun processHtmlAndSave(html: String, context: Context) {
    val doc = Jsoup.parse(html)
    val arr = JSONArray()

    val items = doc.select("div.col.col-12.col-sm-6.col-md-4.col-lg-3")

    items.forEach { el ->
        val rawHeadline = el.selectFirst("div.epaper__headline")?.text()?.trim() ?: ""
        val parts = rawHeadline.split("|")
        val dateStr = parts.getOrNull(0)?.trim()
        val issuePart = parts.getOrNull(1)?.trim()

        val sdfIn = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        val sdfOut = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val publishedDate = dateStr?.let {
            try { sdfOut.format(sdfIn.parse(it)!!) } catch (e: Exception) { it }
        } ?: ""

        val year = dateStr?.takeLast(4) ?: ""
        val issueNumber = issuePart
            ?.substringAfter("Ausgabe-Nr.")
            ?.trim()
            ?.replace("\\s+".toRegex(), "")
            ?: ""

        val title = el.selectFirst("div.epaper__text")?.text()?.trim() ?: ""
        val imgSrc = el.selectFirst("div.epaper__image img")?.attr("src") ?: ""

        val obj = JSONObject().apply {
            put("title", title)
            put("date_published", publishedDate)
            put("year_published", year)
            put("cover_image", imgSrc)
            put("ausgabe_number", issueNumber)
        }
        arr.put(obj)
    }

    // Write to internal storage
    context.openFileOutput("ausgaben.json", Context.MODE_PRIVATE).use { fos ->
        fos.write(arr.toString(2).toByteArray())
    }
}
