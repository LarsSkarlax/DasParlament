package com.lrs.dasparlament.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lrs.dasparlament.R
import com.lrs.dasparlament.databinding.FragmentSettingsBinding
import java.util.*
import androidx.core.content.edit

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Dark Mode Switch Setup
        val darkModeSwitch = binding.switchDarkMode // Annahme: ID ist switch_dark_mode

        // Hole aktuelle Einstellung
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        darkModeSwitch.isChecked = prefs.getBoolean("dark_mode", false)

        // Listener für Umschalten
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // Sprachwahl Dropdown Setup
        val languageDropdown: AutoCompleteTextView = binding.languageDropdown
        val languages = listOf(
            getString(R.string.language_german),
            getString(R.string.language_english)
        )
        val languageCodes = listOf("de", "en")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        languageDropdown.setAdapter(adapter)

        // Vorbelegen: aktuelle Sprache
        val currentLocale = Locale.getDefault().language
        languageDropdown.setText(
            when (currentLocale) {
                "de" -> getString(R.string.language_german)
                "en" -> getString(R.string.language_english)
                else -> getString(R.string.language_german)
            }, false
        )

        languageDropdown.setOnItemClickListener { _, _, position, _ ->
            val languageCode = languageCodes[position]
            setLocale(languageCode)
            // Activity neu starten, damit Sprache übernommen wird
            requireActivity().finish()
            requireActivity().startActivity(requireActivity().intent)
        }

        return root
    }

    private fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        // Optional: In SharedPreferences speichern, wenn App-Sprache dauerhaft behalten werden soll
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.edit { putString("app_language", language) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}