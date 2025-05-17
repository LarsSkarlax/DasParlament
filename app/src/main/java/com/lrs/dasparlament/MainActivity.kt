package com.lrs.dasparlament

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.lrs.dasparlament.databinding.ActivityMainBinding
import com.lrs.dasparlament.downloadPdf
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_notifications
            )
        )
        navView.setupWithNavController(navController)


        binding.fab.setOnClickListener { view ->
            lifecycleScope.launch {
                doSomething()
            }
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()

        }


    }

    private suspend fun doSomething() {
        downloadPdf(
            fileUrl = "https://www.das-parlament.de/epaper/2025/4_5/epaper/ausgabe.pdf",
            context = this,
            folderName = "ausgaben",
            fileName = "4_5.pdf"
        )
    }
}