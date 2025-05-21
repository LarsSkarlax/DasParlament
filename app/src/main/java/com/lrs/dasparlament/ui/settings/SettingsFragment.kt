package com.lrs.dasparlament.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lrs.dasparlament.databinding.FragmentSettingsBinding
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}