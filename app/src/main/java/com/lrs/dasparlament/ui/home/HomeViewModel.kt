// HomeViewModel.kt
package com.lrs.dasparlament.ui.home

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lrs.dasparlament.downloadPdf
import com.lrs.dasparlament.getUrl
import com.lrs.dasparlament.readAusgabenFromAssets
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Data class holding title, subtitle, year and ausgabeNumber for each PDF
    data class PdfItem(
        val title: String,
        val subtitle: String,
        val year: Int,
        val ausgabeNumber: String,
        val coverImageUrl: String
    )

    // LiveData list of PdfItem objects (two-line entries)
    private val _pdfItems = MutableLiveData<List<PdfItem>>()
    val pdfItems: LiveData<List<PdfItem>> = _pdfItems

    // LiveData to trigger opening of a downloaded PDF
    private val _openPdfEvent = MutableLiveData<File?>()
    val openPdfEvent: LiveData<File?> get() = _openPdfEvent

    // LiveData for Snackbar messages
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> get() = _snackbarMessage

    init {
        loadPdfItems()
    }

    fun makeAbsoluteUri(relative: String): String {
        val baseUri = Uri.parse("https://www.das-parlament.de")
        Log.d("makeAbsoluteUri", "Input: $relative")

        val result = when {
            relative.startsWith("http", true) -> {
                Log.d("makeAbsoluteUri", "Detected full URL: $relative")
                relative
            }
            relative.startsWith("//") -> {
                val fullUri = "https:$relative"
                Log.d("makeAbsoluteUri", "Protocol-relative URL detected. Converted to: $fullUri")
                fullUri
            }
            else -> {
                val builtUri = baseUri.buildUpon()
                    .encodedPath(relative.trimStart('/'))
                    .build()
                    .toString()
                Log.d("makeAbsoluteUri", "Relative path converted to: $builtUri")
                builtUri
            }
        }

        Log.d("makeAbsoluteUri", "Result: $result")
        return result
    }





    /**
     * Load PDF titles and subtitles into _pdfItems using the new JSON model.
     */


    private fun loadPdfItems() {
        val context = getApplication<Application>()
        // Read all Ausgaben from assets
        val ausgaben = readAusgabenFromAssets(context)  // List<Ausgabe>
        // Map to PdfItem
        val list = ausgaben.map { a ->
            PdfItem(
                title = a.title,
                subtitle = "${a.yearPublished}:${a.ausgabeNumber}",
                year = a.yearPublished,
                ausgabeNumber = a.ausgabeNumber,
                coverImageUrl  = makeAbsoluteUri(a.cover_image)
            )
        }
        val sorted = list.sortedWith(
            compareByDescending<PdfItem> { it.year }
                .thenByDescending { item ->
                    // Extract numeric prefix, e.g., "10a" -> 10, "02" -> 2
                    item.ausgabeNumber.takeWhile { it.isDigit() }.toIntOrNull() ?: 0
                }
                .thenByDescending { it.ausgabeNumber }
        )
        _pdfItems.postValue(sorted)
    }

    /**
     * Called when the user selects an item at [index].
     * Downloads then opens the PDF with that title.
     */
    fun onPdfSelected(index: Int) {
        val selected = _pdfItems.value?.getOrNull(index)
        if (selected == null) {
            _snackbarMessage.value = "Selected PDF not found."
            return
        }

        // Use the year and ausgabeNumber directly
        val fileUrl = getUrl(selected.ausgabeNumber, selected.year)
        Log.d("MyApp", "File URL: $fileUrl")
        val folder = "parlament_pdfs"
        val filename = "${selected.title}.pdf"

        viewModelScope.launch {
            _snackbarMessage.value = "Downloading Ausgabe ${selected.title}..."
            val downloaded = downloadPdf(
                context = getApplication(),
                fileUrl = fileUrl,
                folderName = folder,
                fileName = filename
            )
            if (downloaded != null) {
                _openPdfEvent.value = downloaded
                _snackbarMessage.value = "Ausgabe ${selected.title} downloaded!"
            } else {
                _snackbarMessage.value = "Failed to download ${selected.title}."
            }
        }
    }

    /**
     * Clear the open-PDF event to avoid re-triggering.
     */
    fun doneOpeningPdf() {
        _openPdfEvent.value = null
    }

    /**
     * Clear the Snackbar message after displaying.
     */
    fun doneShowingSnackbar() {
        _snackbarMessage.value = null
    }

    fun refreshHomeList() {
        loadPdfItems()
        Log.d("CALLED", "refreshHomeList called successfully")
    }

}