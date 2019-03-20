package com.example.fitaware

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import com.example.fitaware.home.HomeFragment


class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "FitAwear"

                val homeFragment = HomeFragment()
                val manager = supportFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.content, homeFragment).addToBackStack(null).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_discover -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Discover"

                val discoverFragment = DiscoverFragment()
                val manager = supportFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.content, discoverFragment).addToBackStack(null).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Awards"

                val awardsFragment = AwardsFragment()
                val manager = supportFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.content, awardsFragment).addToBackStack(null).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_me -> {
//                val actionBar = supportActionBar
//                actionBar?.hide()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = ""

                val meFragment = MeFragment()
                val manager = supportFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.content, meFragment).addToBackStack(null).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu to use in the action bar
//        val inflater = menuInflater
//        inflater.inflate(R.menu.tool_bar, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//        R.id.share -> {
//            // User chose the "Settings" item, show the app settings UI...
//            true
//        }
//
//        else -> {
//            // If we got here, the user's action was not recognized.
//            // Invoke the superclass to handle it.
//            super.onOptionsItemSelected(item)
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar!!.title = ""


        val homeFragment = HomeFragment()
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        transaction.add(R.id.content, homeFragment).addToBackStack(null).commit()
    }


}
