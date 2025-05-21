package com.lrs.dasparlament

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.lrs.dasparlament.databinding.ActivityMainBinding
import com.lrs.dasparlament.ui.home.HomeViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustStatusBarIconsToTheme(this)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_settings
            )
        )
        navView.setupWithNavController(navController)


        binding.fab.setOnClickListener { view ->
            lifecycleScope.launch {
                downloadHtml("https://www.das-parlament.de/e-paper") { html ->
                    // Log and process the HTML
                    Log.d("HTML is downloaded", html.take(500)) // Preview first 500 chars

                    // Call the function to extract & save JSON
                    processHtmlAndSave(html, context = this@MainActivity)
                    Log.d("UPDATE", "NEXT IS REFRESH")
                    Log.d("UPDATE", "NEXT IS REFRESH")
                    Log.d("UPDATE", "NEXT IS REFRESH")
                    homeViewModel.refreshHomeList()
                    Snackbar.make(view, "Ausgaben aktualisiert!", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .show()
                }
                Log.d("CLOSING", "LIFECYCLESCOPE FINISHED")
            }
            Snackbar.make(view, "Updating...", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()

        }


    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


}