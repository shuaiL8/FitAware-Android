package com.example.fitaware.Team


import android.app.Activity
import android.os.Bundle
import android.support.design.widget.TabItem
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.fitaware.R
import android.support.design.widget.TabLayout



class TeamFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(
            R.layout.fragment_team, container,
            false)
        setHasOptionsMenu(false)



        val tabLayoutTeam = view.findViewById<TabLayout>(R.id.tabLayoutTeam)



        val allTeams = tabLayoutTeam.newTab() // Create a new Tab names "First Tab"
        allTeams.text = "All Teams" // set the Text for the first Tab
        tabLayoutTeam.addTab(allTeams, 0)

        val myTeam = tabLayoutTeam.newTab() // Create a new Tab names "First Tab"
        myTeam.text = "My Team" // set the Text for the first Tab
        tabLayoutTeam.addTab(myTeam, 1)

        tabLayoutTeam.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // called when tab selected
                if (tab.position == 0) {
                    Navigation.findNavController(context as Activity, R.id.my_nav_team_fragment).navigate(R.id.allTeamsFragment)
                } else {
                    Navigation.findNavController(context as Activity, R.id.my_nav_team_fragment).navigate(R.id.myTeamFragment)
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

}
