package com.lrs.dasparlament

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Downloads a PDF from the given URL and saves it to a specified folder
 * in the app's external files directory.
 *
 * @param context The application context.
 * @param fileUrl The URL of the PDF file to download.
 * @param folderName The name of the folder within the app's external files
 * directory where the PDF will be saved (e.g., "MyDownloads").
 * @param fileName The desired name for the saved PDF file (e.g., "document.pdf").
 * If null, it will try to extract the filename from the URL.
 * @return The File object of the saved PDF, or null if the download failed.
 */
suspend fun downloadPdf(
    context: Context,
    fileUrl: String,
    folderName: String,
    fileName: String? = null
): File? {
    return withContext(Dispatchers.IO) { // Perform network and file operations on a background thread
        var connection: HttpURLConnection? = null
        var fos: FileOutputStream? = null
        var downloadedFile: File? = null

        try {
            val url = URL(fileUrl)
            connection = url.openConnection() as HttpURLConnection

            // *** OPTIONAL: Set connection timeouts for large files ***
            connection.connectTimeout = 15000 // 15 seconds for connection establishment
            connection.readTimeout = 30000 // 30 seconds for reading data

            connection.connect()

            // Check for successful response
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                println("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
                return@withContext null
            }

            // *** NEW: Log Content-Length ***
            val contentLength = connection.contentLength
            if (contentLength > 0) {
                println("Expected file size (Content-Length): ${contentLength / 1024} KB")
            } else {
                println("Content-Length header not found or is 0.")
            }


            // Determine the output filename
            val outputFileName = fileName ?: fileUrl.substring(fileUrl.lastIndexOf('/') + 1)
            if (outputFileName.isEmpty() || !outputFileName.endsWith(".pdf", ignoreCase = true)) {
                println("Could not determine a valid PDF filename.")
                return@withContext null // Or provide a default name like "downloaded.pdf"
            }

            // Get the directory for the app's private files on external storage.
            // This is a good place to store files that are private to your app but
            // should be available even if the app is uninstalled (unless cleared by user).
            // No special permissions are needed for this location on Android 4.4+.
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), folderName)

            // Create the directory if it doesn't exist
            if (!downloadDir.exists()) {
                if (!downloadDir.mkdirs()) {
                    println("Failed to create directory: ${downloadDir.absolutePath}")
                    return@withContext null
                }
            }

            downloadedFile = File(downloadDir, outputFileName)
            fos = FileOutputStream(downloadedFile)

            val inputStream = connection.inputStream
            val buffer = ByteArray(1024)
            var len1: Int
            var totalBytesRead = 0L // To track progress

            while (inputStream.read(buffer).also { len1 = it } != -1) {
                fos.write(buffer, 0, len1)
                totalBytesRead += len1
                // *** NEW: Log progress periodically (e.g., every 1MB) ***
                if (contentLength > 0 && totalBytesRead % (1024 * 1024) == 0L) { // Log every 1MB
                    println("Downloaded ${totalBytesRead / (1024 * 1024)} MB of ${contentLength / (1024 * 1024)} MB")
                }
            }

            println("PDF downloaded successfully to: ${downloadedFile.absolutePath}")
            println("Actual downloaded size: ${downloadedFile.length() / 1024} KB") // Verify final size

            // *** NEW: Compare expected vs. actual size ***
            if (contentLength > 0 && downloadedFile.length() != contentLength.toLong()) {
                println("WARNING: Downloaded size (${downloadedFile.length()} bytes) does not match expected size (${contentLength} bytes).")
            }

            return@withContext downloadedFile

        } catch (e: IOException) {
            e.printStackTrace()
            println("Error downloading PDF: ${e.message}")
            // Clean up partially downloaded file if an error occurs
            downloadedFile?.delete()
            return@withContext null
        } finally {
            fos?.close()
            connection?.disconnect()
        }
    }
}

/**
 * Example of how to get a URL from another function.
 * Replace this with your actual function.
 */
fun getPdfUrl(): String {
    // In a real scenario, this might fetch the URL from a server, user input, etc.
    // Make sure this URL is for your 10MB PDF.
    return "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf" // A sample PDF URL (this is a small one!)
    // If your 10MB PDF is on a different server, replace this.
    // Example: "https://example.com/large_document.pdf"
}