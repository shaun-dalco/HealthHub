package com.hotmail.shaundalco.healthhub

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hotmail.shaundalco.healthhub.ui.AddBottomSheet
import com.hotmail.shaundalco.healthhub.ui.HeartRateLastMonthFragment
import com.hotmail.shaundalco.healthhub.ui.HomeFragment
import com.hotmail.shaundalco.healthhub.ui.SettingsFragment
import com.hotmail.shaundalco.healthhub.ui.StepsLastMonthFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navDashboard: ImageButton
    private lateinit var navHealth: ImageButton
    private lateinit var navAdd: ImageButton
    private lateinit var navFitness: ImageButton
    private lateinit var navSettings: ImageButton

    private var currentTab: Tab = Tab.DASHBOARD

    enum class Tab { DASHBOARD, HEALTH, ADD, FITNESS, SETTINGS }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navDashboard = findViewById(R.id.navDashboard)
        navHealth = findViewById(R.id.navHealth)
        navAdd = findViewById(R.id.navAdd)
        navFitness = findViewById(R.id.navFitness)
        navSettings = findViewById(R.id.navSettings)

        navDashboard.setOnClickListener { openTab(Tab.DASHBOARD) }
        navHealth.setOnClickListener { openTab(Tab.HEALTH) }
        navAdd.setOnClickListener { openTab(Tab.ADD) } // opens sheet
        navFitness.setOnClickListener { openTab(Tab.FITNESS) }
        navSettings.setOnClickListener { openTab(Tab.SETTINGS) }

        if (savedInstanceState == null) {
            openTab(Tab.DASHBOARD, addToBackStack = false)
        } else {
            currentTab = Tab.valueOf(
                savedInstanceState.getString("currentTab", Tab.DASHBOARD.name)
            )
            setSelectedNav(currentTab)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fm = supportFragmentManager
                if (fm.backStackEntryCount > 0) {
                    fm.popBackStack()
                } else {
                    if (currentTab != Tab.DASHBOARD) {
                        openTab(Tab.DASHBOARD, addToBackStack = false)
                    } else {
                        finish()
                    }
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("currentTab", currentTab.name)
        super.onSaveInstanceState(outState)
    }

    private fun openTab(tab: Tab, addToBackStack: Boolean = false) {
        // ADD is not a "tab" fragment – it opens a bottom sheet
        if (tab == Tab.ADD) {
            AddBottomSheet().show(supportFragmentManager, "AddBottomSheet")
            // Keep selection on whatever tab was active
            setSelectedNav(currentTab)
            return
        }

        if (tab == currentTab && supportFragmentManager.findFragmentById(R.id.fragmentHost) != null) {
            return
        }

        val fragment: Fragment = when (tab) {
            Tab.DASHBOARD -> HomeFragment()
            Tab.HEALTH -> StepsLastMonthFragment()
            Tab.FITNESS -> HeartRateLastMonthFragment() // swap later
            Tab.SETTINGS -> SettingsFragment()
            Tab.ADD -> throw IllegalStateException("ADD handled above")
        }

        currentTab = tab
        setSelectedNav(tab)

        val tx = supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragmentHost, fragment, tab.name)

        if (addToBackStack) tx.addToBackStack(tab.name)
        tx.commit()
    }

    private fun setSelectedNav(tab: Tab) {
        val dim = 0x99FFFFFF.toInt()
        val bright = Color.WHITE

        navDashboard.setColorFilter(dim)
        navHealth.setColorFilter(dim)
        navFitness.setColorFilter(dim)
        navSettings.setColorFilter(dim)

        // + is always bright
        navAdd.setColorFilter(bright)

        when (tab) {
            Tab.DASHBOARD -> navDashboard.setColorFilter(bright)
            Tab.HEALTH -> navHealth.setColorFilter(bright)
            Tab.FITNESS -> navFitness.setColorFilter(bright)
            Tab.SETTINGS -> navSettings.setColorFilter(bright)
            Tab.ADD -> { /* not used as currentTab */ }
        }
    }
}
