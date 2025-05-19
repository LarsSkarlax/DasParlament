package com.lrs.dasparlament.ui.home

import android.app.Application
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.lrs.dasparlament.R
import com.lrs.dasparlament.downloadPdf
import com.lrs.dasparlament.getEventAtIndexForYear
import com.lrs.dasparlament.getEventCountForYear
import com.lrs.dasparlament.getUrl
import kotlinx.coroutines.launch // Import launch for coroutines
import java.io.File // Import File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Expose an immutable LiveData list of PDF names
    private val _pdfNames = MutableLiveData<List<String>>()
    val pdfNames: LiveData<List<String>> = _pdfNames

    // LiveData to signal when a PDF is ready to be opened
    private val _openPdfEvent = MutableLiveData<File?>()
    val openPdfEvent: LiveData<File?>
        get() = _openPdfEvent

    // LiveData for Snackbar messages (from our previous conversation)
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?>
        get() = _snackbarMessage


    init {
        loadPdfNames()
    }

    private fun loadPdfNames() {
        val count = getEventCountForYear(context = getApplication(), 2025)
        val names = List(count) { index -> getEventAtIndexForYear(context = getApplication(), targetYear = 2025, index = index) }
        _pdfNames.value = names as List<String>?
    }


    /**
     * This is called by the Fragment when an item is clicked.
     * It initiates the download and signals the UI to open the PDF.
     */
    fun onPdfSelected(index: Int) {
        // Ensure the list is not null and the index is valid
        val selectedPdfName = _pdfNames.value?.getOrNull(index)

        if (selectedPdfName != null) {
            val fileUrl = getUrl(selectedPdfName) // Get the URL from the PDF name
            val folderName = "parlament_pdfs" // A folder name for your PDFs
            val fileName = "$selectedPdfName.pdf" // Ensure a .pdf extension

            // Use viewModelScope to launch a coroutine for the download
            viewModelScope.launch {
                // Show a Snackbar message that download has started
                _snackbarMessage.value = "Downloading $selectedPdfName..."

                val downloadedFile = downloadPdf(
                    context = getApplication(), // Use Application Context
                    fileUrl = fileUrl,
                    folderName = folderName,
                    fileName = fileName
                )

                if (downloadedFile != null) {
                    // Signal the UI to open this downloaded file
                    _openPdfEvent.value = downloadedFile
                    _snackbarMessage.value = "$selectedPdfName downloaded!"
                } else {
                    // Handle download failure
                    _snackbarMessage.value = "Failed to download $selectedPdfName."
                }
            }
        } else {
            _snackbarMessage.value = "Selected PDF not found."
        }
    }

    /**
     * Call this from your UI after the PDF has been successfully opened
     * to clear the event and prevent re-triggering.
     */
    fun doneOpeningPdf() {
        ;
    }

    /**
     * Call this from your UI after the Snackbar message has been shown
     * to clear the event and prevent re-triggering.
     */
    fun doneShowingSnackbar() {
        _snackbarMessage.value = null
    }
}