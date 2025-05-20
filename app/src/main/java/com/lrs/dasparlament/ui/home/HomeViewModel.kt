
// HomeViewModel.kt
package com.lrs.dasparlament.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lrs.dasparlament.downloadPdf
import com.lrs.dasparlament.getEventAtIndexForYear
import com.lrs.dasparlament.getEventCountForYear
import com.lrs.dasparlament.getUrl
import com.lrs.dasparlament.getYearAtIndex
import com.lrs.dasparlament.getYearCount
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Data class holding title and subtitle for each PDF
    data class PdfItem(val title: String, val subtitle: String)

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

    /**
     * Load PDF titles and subtitles into _pdfItems.
     * Here subtitle is derived from the download URL for demonstration.
     */
    private fun loadPdfItems() {
        val context = getApplication<Application>()
        val yearCount = getYearCount(context)
        val list = mutableListOf<PdfItem>()

        // Loop through years in original order (e.g., 2025, 2024, ...)
        for (i in 0 until yearCount) {
            val year = getYearAtIndex(context, i) ?: continue
            val eventCount = getEventCountForYear(context, year)

            // Loop through events in reverse order (last event first)
            for (index in eventCount - 1 downTo 0) {
                val event = getEventAtIndexForYear(context, year, index)

                val title = "$year: $index"       // e.g., "2025: 3"
                val subtitle = event.toString()   // e.g., "10_11"
                list.add(PdfItem(title, subtitle))
            }
        }

        _pdfItems.value = list
    }





    /**
     * Called when the user selects an item at [index].
     * Downloads then opens the PDF with that title.
     */
    fun onPdfSelected(index: Int) {
        // Safely retrieve the title from _pdfItems
        val selected = _pdfItems.value?.getOrNull(index)
        if (selected == null) {
            _snackbarMessage.value = "Selected PDF not found."
            return
        }

        // Prepare download params
        val year = selected.title.split(":")[0]
        val fileUrl = getUrl(selected.subtitle, year)
        Log.d("MyApp", "File URL: $fileUrl")
        Log.d("MyApp", "File URL: $fileUrl")
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
}
