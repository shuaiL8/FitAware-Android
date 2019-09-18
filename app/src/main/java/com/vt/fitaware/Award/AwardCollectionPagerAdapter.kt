package com.vt.fitaware.Award

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class AwardCollectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {



    override fun getCount(): Int  = 2

    override fun getItem(i: Int): Fragment {
        if(i == 1) {
            val teamAwardFragment = TeamAwardFragment()
            teamAwardFragment.arguments = Bundle().apply {
                // Our object is just an integer :-P
                putInt("object", i + 1)
            }
            return teamAwardFragment
        }
        else {
            val personalAwardFragment = PersonalAwardFragment()
            personalAwardFragment.arguments = Bundle().apply {
                // Our object is just an integer :-P
                putInt("object", i + 1)
            }
            return personalAwardFragment
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            1 -> "Team"
            else -> "Personal"
        }
    }
}