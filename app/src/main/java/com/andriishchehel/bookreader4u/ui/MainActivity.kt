package com.andriishchehel.bookreader4u.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.andriishchehel.bookreader4u.R
import com.andriishchehel.bookreader4u.ui.library.LibraryFragment
import com.andriishchehel.bookreader4u.ui.library.saved.LibrarySavedFragment
import com.andriishchehel.bookreader4u.databinding.ActivityMainBinding
import com.andriishchehel.bookreader4u.ui.home.HomeFragment
import com.andriishchehel.bookreader4u.ui.search.SearchFragment
import com.andriishchehel.bookreader4u.ui.settings.SettingsFragment
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNav.setOnItemSelectedListener(this)
        binding.bottomNav.selectedItemId = R.id.nav_home
    }

    override fun onNavigationItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.nav_home -> onHomeClicked()
        R.id.nav_search -> onSearchClicked()
        R.id.nav_library -> onLibraryClicked()
        R.id.nav_settings -> onSettingsClicked()
        else -> false
    }

    private fun onHomeClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, HomeFragment())
        }
        return true
    }

    private fun onSearchClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, SearchFragment())
        }
        return true
    }

    private fun onLibraryClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, LibraryFragment())
        }
        return true
    }

    private fun onSettingsClicked(): Boolean {
        supportFragmentManager.commit {
            replace(R.id.frame_content, SettingsFragment())
        }
        return true
    }
}