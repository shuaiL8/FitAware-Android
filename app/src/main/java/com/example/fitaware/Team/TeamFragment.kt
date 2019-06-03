package com.example.fitaware.Team


import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TabItem
import android.support.v4.app.Fragment
import androidx.navigation.Navigation
import com.example.fitaware.R
import android.support.design.widget.TabLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_team.*


class TeamFragment : Fragment() {
    private val TAG = "AllTeamsFragment"

    private var sharedPreferences: SharedPreferences? = null
    private var selectTab = "all"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(
            R.layout.fragment_team, container,
            false)
        setHasOptionsMenu(true)
        initSharedPreferences()

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = "Team"


        val tabLayoutTeam = view.findViewById<TabLayout>(R.id.tabLayoutTeam)

        val tabLayoutPeriodical = view.findViewById<TabLayout>(R.id.tabLayoutPeriodical)

        val allTeams = tabLayoutTeam.newTab()
        allTeams.setIcon(R.drawable.ic_grid)
        tabLayoutTeam.addTab(allTeams, 0)

        val myTeam = tabLayoutTeam.newTab()
        myTeam.setIcon(R.drawable.ic_list)
        tabLayoutTeam.addTab(myTeam, 1)


        tabLayoutTeam.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // called when tab selected
                if (tab.position == 0) {
                    Navigation.findNavController(context as Activity, R.id.my_nav_team_fragment).navigate(R.id.allTeamsFragment)


                } else {
                    Navigation.findNavController(context as Activity, R.id.my_nav_team_fragment).navigate(R.id.allTeamsFragment_list)

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // called when tab unselected
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // called when a tab is reselected
            }
        })

        tabLayoutPeriodical.addTab(tabLayoutPeriodical.newTab().setText("all"))
        tabLayoutPeriodical.addTab(tabLayoutPeriodical.newTab().setText("daily"))
        tabLayoutPeriodical.addTab(tabLayoutPeriodical.newTab().setText("3 days"))
        tabLayoutPeriodical.addTab(tabLayoutPeriodical.newTab().setText("5 days"))
        tabLayoutPeriodical.addTab(tabLayoutPeriodical.newTab().setText("weekly"))



        selectTab = sharedPreferences!!.getString("tabLayoutPeriodical", "0")// called when a tab is reselected

        // called when tab selected
        when (selectTab) {
            "all" -> tabLayoutPeriodical.getTabAt(0)!!.select()
            "daily" -> tabLayoutPeriodical.getTabAt(1)!!.select()
            "3 days" -> tabLayoutPeriodical.getTabAt(2)!!.select()
            "5 days" -> tabLayoutPeriodical.getTabAt(3)!!.select()
            "weekly" -> tabLayoutPeriodical.getTabAt(4)!!.select()
        }

        Log.i(TAG, "tabLayoutPeriodical: "+ tabLayoutPeriodical.selectedTabPosition)


        tabLayoutPeriodical.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // called when tab selected
                val editor = sharedPreferences?.edit()

                if (tab.position == 0) {
                    selectTab = "all"
                    Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                }
                else if (tab.position == 1) {
                    selectTab = "daily"
                    Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                }
                else if (tab.position == 2) {
                    selectTab = "3 days"
                    Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                }
                else if (tab.position == 3){
                    selectTab = "5 days"
                    Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                }
                else if (tab.position == 4){
                    selectTab = "weekly"
                    Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)
                }
                editor!!.putString("tabLayoutPeriodical", selectTab)
                editor.commit()


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
        inflater.inflate(R.menu.tool_bar2, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.add -> {
            // User chose the "Settings" item, show the app settings UI...
            Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.createNewTeamFragment)

            true
        }


        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

}
