package com.onestackdev.katrina.screens.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.onestackdev.katrina.R
import com.onestackdev.katrina.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var navHost: NavHostFragment
    private lateinit var navController: NavController

    companion object {
        lateinit var activity: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        activity = this
        onBackPressHandel()

        MobileAds.initialize(this)

        setupNavigationComponent()

    }

    private fun setupNavigationComponent() {
        navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        navController = navHost.navController

        binding.bottomNavMain.setupWithNavController(navController)
    }

    private fun onBackPressHandel() {
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }
}