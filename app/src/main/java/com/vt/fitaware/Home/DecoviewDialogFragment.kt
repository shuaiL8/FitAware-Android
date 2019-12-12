package com.vt.fitaware.Home

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vt.fitaware.R

class DecoviewDialogFragment : DialogFragment() {

    private val TAG = "DecoviewDialogFragment"
    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_bargraphs_dialog, container,
            false
        )
        initSharedPreferences()


        return view
    }



    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }
}