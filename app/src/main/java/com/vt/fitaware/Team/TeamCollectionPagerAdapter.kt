package com.vt.fitaware.Team

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class TeamCollectionPagerAdapter(fm: FragmentManager, gOrL: String) : FragmentStatePagerAdapter(fm) {

    private var gridOrList = gOrL


    override fun getCount(): Int  = 5

    override fun getItem(i: Int): Fragment {
        if(gridOrList == "grid") {
            val allTeamsFragment = AllTeamsFragment()
            allTeamsFragment.arguments = Bundle().apply {
                // Our object is just an integer :-P
                putInt("object", i + 1)
            }
            return allTeamsFragment
        }
        else{
            val allTeamsListFragment = AllTeamsListFragment()
            allTeamsListFragment.arguments = Bundle().apply {
                // Our object is just an integer :-P
                putInt("object", i + 1)
            }
            return allTeamsListFragment
        }

    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> "Daily"
            2 -> "3 Days"
            3 -> "5 Days"
            4 -> "Weekly"
            else -> "All"
        }
    }
}