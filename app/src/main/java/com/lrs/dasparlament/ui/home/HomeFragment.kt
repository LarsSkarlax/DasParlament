package com.lrs.dasparlament.ui.home

import android.content.Intent // Import Intent
import android.net.Uri      // Import Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider // Import FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe // Use androidx.lifecycle.Observer for explicit import if needed
import com.google.android.material.snackbar.Snackbar // Import Snackbar
import com.lrs.dasparlament.R
import com.lrs.dasparlament.databinding.FragmentHomeBinding
import android.util.Log // For debugging logs

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Using by viewModels() to get the ViewModel instance
    // ViewModelProvider will automatically use AndroidViewModelFactory because MyViewModel extends AndroidViewModel
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHomeBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // Call super.onViewCreated

        // Observe the list of PDF names (this part was already there and working)
        viewModel.pdfNames.observe(viewLifecycleOwner) { names ->
            binding.pdfListView.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                names
            )
        }

        // On click, delegate back to ViewModel (this part was already there and working)
        binding.pdfListView.setOnItemClickListener { _, _, pos, _ ->
            viewModel.onPdfSelected(pos)
        }

        // --- NEW: Observe for Snackbar messages ---
        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG) // Use binding.root as the anchor view
                    // .setAnchorView(R.id.fab) // If you have a FAB, uncomment this and ensure R.id.fab is in your layout
                    .show()
                viewModel.doneShowingSnackbar() // Consume the event
                Log.d("PDF_VIEWER", "Snackbar message shown: $it")
            }
        }

        // --- NEW: Observe for PDF open events ---
        viewModel.openPdfEvent.observe(viewLifecycleOwner) { file ->
            Log.d("PDF_VIEWER", "openPdfEvent observed. File: $file") // Debug log

            file?.let { pdfFile ->
                try {
                    // Get Uri from FileProvider
                    val pdfUri: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider", // Authority must match manifest
                        pdfFile
                    )
                    Log.d("PDF_VIEWER", "FileProvider URI generated: $pdfUri") // Debug log

                    // Create Intent to view the PDF
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(pdfUri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant temp permission to viewer
                    }

                    // Check if there's an app to handle this intent
                    val resolvedActivity = intent.resolveActivity(requireContext().packageManager)
                    Log.d("PDF_VIEWER", "Resolved Activity: $resolvedActivity") // Debug log

                    if (resolvedActivity != null) {
                        startActivity(intent)
                        Log.d("PDF_VIEWER", "startActivity called for PDF.") // Debug log
                    } else {
                        Log.w("PDF_VIEWER", "No PDF viewer app found on device.") // Debug log
                        Snackbar.make(binding.root, "No PDF viewer app found.", Snackbar.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Log.e("PDF_VIEWER", "Error opening PDF: ${e.message}", e) // Debug log
                    Snackbar.make(binding.root, "Error opening PDF: ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
                } finally {
                    viewModel.doneOpeningPdf() // Consume the event even if there was an error
                    Log.d("PDF_VIEWER", "doneOpeningPdf called.") // Debug log
                }
            } ?: run { // If file is null
                Log.d("PDF_VIEWER", "Observed file is null (shouldn't happen on success).")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}