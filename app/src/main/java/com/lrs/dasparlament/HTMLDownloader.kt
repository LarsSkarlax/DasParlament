package com.lrs.dasparlament

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Function to download HTML and pass it to another function
suspend fun downloadHtml(url: String, onHtmlReceived: (String) -> Unit) {
    val client = OkHttpClient()

    withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val html = response.body?.string() ?: ""
                onHtmlReceived(html)
            } else {
                onHtmlReceived("Error: ${response.code}")
            }
        } catch (e: Exception) {
            onHtmlReceived("Exception: ${e.message}")
        }
    }
}
