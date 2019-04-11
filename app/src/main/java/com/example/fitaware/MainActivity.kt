package com.example.fitaware

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import androidx.navigation.Navigation


class MainActivity : AppCompatActivity() {



    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

            R.id.navigation_home -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "FitAwear"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.homeFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_me -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Me"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.meFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_team -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Team"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_awards-> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Awards"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.awardsFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
//                val actionBar = supportActionBar
//                actionBar?.hide()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = ""

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.settingFragment)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar!!.title = ""

    }

}
