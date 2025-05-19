
// HomeViewModel.kt
package com.lrs.dasparlament.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lrs.dasparlament.downloadPdf
import com.lrs.dasparlament.getEventAtIndexForYear
import com.lrs.dasparlament.getEventCountForYear
import com.lrs.dasparlament.getUrl
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
        val year = 2025
        val count = getEventCountForYear(getApplication(), year)
        val list = List(count) { index ->
            val title = getEventAtIndexForYear(getApplication(), year, index)
            val subtitle = getUrl(title.toString()) // e.g. display URL or other info
            PdfItem(title.toString(), subtitle)
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
        val fileUrl = getUrl(selected.title)
        val folder = "parlament_pdfs"
        val filename = "${selected.title}.pdf"

        viewModelScope.launch {
            _snackbarMessage.value = "Downloading ${selected.title}..."
            val downloaded = downloadPdf(
                context = getApplication(),
                fileUrl = fileUrl,
                folderName = folder,
                fileName = filename
            )
            if (downloaded != null) {
                _openPdfEvent.value = downloaded
                _snackbarMessage.value = "${selected.title} downloaded!"
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
