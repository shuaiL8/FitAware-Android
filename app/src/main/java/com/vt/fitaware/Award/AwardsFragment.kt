package com.vt.fitaware.Award


import android.app.Activity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.*
import androidx.navigation.Navigation
import com.vt.fitaware.R


class AwardsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_awards, container,
            false)
        setHasOptionsMenu(true)

        val tabLayoutTeam = view.findViewById<TabLayout>(R.id.tabLayoutAwards)

        val myTeam = tabLayoutTeam.newTab() // Create a new Tab names "First Tab"
        myTeam.text = "Personal" // set the Text for the first Tab
        tabLayoutTeam.addTab(myTeam, 0)

        val allTeams = tabLayoutTeam.newTab() // Create a new Tab names "First Tab"
        allTeams.text = "Team" // set the Text for the first Tab
        tabLayoutTeam.addTab(allTeams, 1)

        tabLayoutTeam.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // called when tab selected
                if (tab.position == 0) {
                    Navigation.findNavController(context as Activity, R.id.my_nav_awards_fragment).navigate(R.id.personalAwardFragment)
                } else {
                    Navigation.findNavController(context as Activity, R.id.my_nav_awards_fragment).navigate(R.id.teamAwardFragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // called when tab unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // called when a tab is reselected
            }
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tool_bar, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.share -> {
            // User chose the "Settings" item, show the app settings UI...
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

}
