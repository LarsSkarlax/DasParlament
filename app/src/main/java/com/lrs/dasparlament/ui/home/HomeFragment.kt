package com.lrs.dasparlament.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.artifex.mupdf.viewer.DocumentActivity
import com.google.android.material.snackbar.Snackbar
import com.lrs.dasparlament.databinding.FragmentHomeBinding

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
                    val intent: Intent = Intent(requireContext(), DocumentActivity::class.java)
                    intent.setAction(Intent.ACTION_VIEW)
                    intent.setData(pdfUri)
                    startActivity(intent)

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