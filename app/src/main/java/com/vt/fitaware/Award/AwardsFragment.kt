package com.vt.fitaware.Award


import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.*
import androidx.navigation.Navigation
import com.vt.fitaware.R


class AwardsFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var awardCollectionPagerAdapter: AwardCollectionPagerAdapter
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_awards, container,
            false)
        setHasOptionsMenu(true)
        initSharedPreferences()

        val tabLayoutAwards = view.findViewById<TabLayout>(R.id.tabLayoutAwards)

        awardCollectionPagerAdapter = AwardCollectionPagerAdapter(childFragmentManager)
        viewPager = view.findViewById(R.id.awardPager)
        viewPager.adapter = awardCollectionPagerAdapter
        tabLayoutAwards.setupWithViewPager(viewPager)


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tool_bar, menu)
    }


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

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

}
