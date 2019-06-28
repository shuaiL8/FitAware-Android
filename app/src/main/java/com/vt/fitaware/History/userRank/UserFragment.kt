package com.vt.fitaware

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class userFragment : Fragment() {

    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"
    private var newSelectedDate: String = "none"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_user, container,
            false
        )
        initSharedPreferences()

        user_id = sharedPreferences!!.getString("user_id", "none")
        newSelectedDate = sharedPreferences!!.getString("newSelectedDate", "none")


        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = "$newSelectedDate's Rank"



        return view
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

}
