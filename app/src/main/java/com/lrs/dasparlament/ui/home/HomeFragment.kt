package com.lrs.dasparlament.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.artifex.mupdf.viewer.DocumentActivity
import com.google.android.material.snackbar.Snackbar
import com.lrs.dasparlament.R
import com.lrs.dasparlament.databinding.FragmentHomeBinding
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Using by viewModels() to get the ViewModel instance
    // ViewModelProvider will automatically use AndroidViewModelFactory because MyViewModel extends AndroidViewModel
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentHomeBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe two-line PDF items

        viewModel.pdfItems.observe(viewLifecycleOwner) { items ->
            val adapter = object : ArrayAdapter<HomeViewModel.PdfItem>(
                requireContext(),
                R.layout.list_item_two_line,  // hier dein custom XML
                items
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val row = convertView ?: LayoutInflater.from(context)
                        .inflate(R.layout.list_item_two_line, parent, false)

                    val item = getItem(position)!!
                    row.findViewById<TextView>(R.id.text1).text = item.title
                    row.findViewById<TextView>(R.id.text2).text = item.subtitle

                    val imageView = row.findViewById<ImageView>(R.id.coverImageView)
                    Glide.with(context)
                        .load(item.coverImageUrl)
                        .placeholder(R.drawable.ic_placeholder) // Eigenes Platzhalter-Drawable
                        .error(R.drawable.ic_error)             // Eigenes Fehler-Drawable
                        .into(imageView)


                    return row
                }
            }
            binding.pdfListView.adapter = adapter
        }


        // On click, delegate back to ViewModel (this part was already there and working)
        binding.pdfListView.setOnItemClickListener { _, _, pos, _ ->
            viewModel.onPdfSelected(pos)
        }

        // --- NEW: Observe for Snackbar messages ---
        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG) // Use binding.root as the anchor view
                    .setAnchorView(R.id.fab) // If you have a FAB, uncomment this and ensure R.id.fab is in your layout
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


                    val fileUri = Uri.fromFile(pdfFile)                // <-- plain file:// URI
                    Log.d("PDF_VIEWER", "Using direct Uri: $fileUri")

                    // Create Intent to view the PDF
                    val intent = Intent(requireContext(), DocumentActivity::class.java)
                    intent.setAction(Intent.ACTION_VIEW)
                    intent.setData(fileUri)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)


                } catch (e: Exception) {
                    Log.e("PDF_VIEWER", "Error opening PDF: ${e.message}", e) // Debug log
                    Snackbar.make(binding.root, "Error opening PDF: Ausgabe ${e.localizedMessage}", Snackbar.LENGTH_LONG).show()
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