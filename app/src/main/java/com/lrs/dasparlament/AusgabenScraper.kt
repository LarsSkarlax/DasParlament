package com.lrs.dasparlament

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Locale
fun processHtmlAndSave(html: String, context: Context) {
    val doc = Jsoup.parse(html)
    val arr = JSONArray()
    val items = doc.select("div.col.col-12.col-sm-6.col-md-4.col-lg-3")

    val sdfIn = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
    val sdfOut = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    items.forEachIndexed { index, el ->
        try {
            val rawHeadline = el.selectFirst("div.epaper__headline")?.text()?.trim().orEmpty()
            val parts = rawHeadline.split("|")
            val dateStr = parts.getOrNull(0)?.trim()
            val issuePart = parts.getOrNull(1)?.trim()

            val publishedDate = dateStr?.let {
                try {
                    sdfOut.format(sdfIn.parse(it)!!)
                } catch (e: Exception) {
                    Log.w("PARSING", "Invalid date format in '$it'", e)
                    null
                }
            }

            val year = dateStr?.takeLast(4)?.takeIf { it.all { c -> c.isDigit() } } ?: ""
            val issueNumber = issuePart
                ?.substringAfter("Ausgabe-Nr.", "")
                ?.trim()
                ?.replace("\\s+".toRegex(), "")
                .orEmpty()

            val title = el.selectFirst("div.epaper__text")?.text()?.trim().orEmpty()
            val imgSrc = el.selectFirst("div.epaper__image img")?.attr("src").orEmpty()

            // Skip entries with any critical missing data
            if (title.isEmpty() || publishedDate.isNullOrEmpty() || issueNumber.isEmpty()) {
                Log.w("SKIPPED", "Skipping item at index $index due to missing data")
                return@forEachIndexed
            }

            val obj = JSONObject().apply {
                put("title", title)
                put("date_published", publishedDate)
                put("year_published", year)
                put("cover_image", imgSrc)
                put("ausgabe_number", issueNumber)
            }

            arr.put(obj)

        } catch (e: Exception) {
            Log.e("PROCESSING", "Failed to process item at index $index", e)
        }
    }

    try {
        context.openFileOutput("ausgaben.json", Context.MODE_PRIVATE).use { fos ->
            fos.write(arr.toString(2).toByteArray())
        }
        Log.d("FINISHED", "Finished HTML processing successfully and wrote to file")
    } catch (e: Exception) {
        Log.e("FILE_WRITE", "Failed to write JSON file", e)
    }
}
