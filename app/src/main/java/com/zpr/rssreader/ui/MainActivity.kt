package com.zpr.rssreader.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.zpr.rssreader.R
import com.zpr.rssreader.databinding.ActivityMainBinding

/**
 * 主界面容器，负责工具栏、导航图与底部导航的联动。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        val topLevelDestinations = setOf(
            R.id.feedListFragment,
            R.id.itemListFragment,
            R.id.favoriteFragment,
            R.id.settingsFragment
        )
        val appBarConfiguration = AppBarConfiguration(topLevelDestinations)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navHostFragment.navController.navigateUp() ||
            super.onSupportNavigateUp()
    }

    /**
     * 供 Fragment 在进入具体订阅源文章列表时覆盖工具栏标题。
     */
    fun setToolbarTitle(title: CharSequence) {
        supportActionBar?.title = title
    }
}
