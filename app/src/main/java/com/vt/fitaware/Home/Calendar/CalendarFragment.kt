package com.vt.fitaware.Home.Calendar

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.vt.fitaware.R
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList



class CalendarFragment : Fragment() {

    lateinit var cal_month: GregorianCalendar
    lateinit var cal_month_copy:GregorianCalendar
    private lateinit var hwAdapter: HwAdapter
    private lateinit var tv_month: TextView
    private val TAG = "CalendarFragment"
    private var date_collection_arr = ArrayList<HomeCollection>(1)

    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_calendar, container,
            false
        )
        setHasOptionsMenu(true)
        initSharedPreferences()

        user_id = sharedPreferences!!.getString("user_id", "none")

        Log.i(TAG, "calender user_id$user_id")

        cal_month = GregorianCalendar.getInstance() as GregorianCalendar
        cal_month_copy = cal_month.clone() as GregorianCalendar
        hwAdapter = HwAdapter(activity, cal_month, date_collection_arr)

        tv_month = view.findViewById(R.id.tv_month)
        tv_month!!.text = android.text.format.DateFormat.format("MMMM yyyy", cal_month)

        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$user_id")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>

                    Log.i(TAG, "myTeamMember: $my")


                    date_collection_arr.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>


                        date_collection_arr.add(
                            HomeCollection(
                                key,
                                details["Minis"], details["Steps"], details["HPs"], details["Ms"], details["Cals"], details["Goal"]
                            )
                        )


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    refreshCalendar()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

        val previous = view.findViewById<View>(R.id.ib_prev) as ImageButton
        previous.setOnClickListener {
            if (cal_month.get(GregorianCalendar.MONTH) == 4 && cal_month.get(GregorianCalendar.YEAR) == 2017) {
                //cal_month.set((cal_month.get(GregorianCalendar.YEAR) - 1), cal_month.getActualMaximum(GregorianCalendar.MONTH), 1);
                Toast.makeText(context, "Event Detail is available for current session only.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                setPreviousMonth()
                refreshCalendar()
            }
        }
        val next = view.findViewById<View>(R.id.Ib_next) as ImageButton
        next.setOnClickListener {
            if (cal_month.get(GregorianCalendar.MONTH) == 5 && cal_month.get(GregorianCalendar.YEAR) == 2018) {
                //cal_month.set((cal_month.get(GregorianCalendar.YEAR) + 1), cal_month.getActualMinimum(GregorianCalendar.MONTH), 1);
                Toast.makeText(context, "Event Detail is available for current session only.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                setNextMonth()
                refreshCalendar()
            }
        }
        val gridview = view.findViewById<View>(R.id.gv_calendar) as GridView
        gridview.adapter = hwAdapter
        gridview.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            val selectedGridDate = HwAdapter.day_string[position]
            (parent.adapter as HwAdapter).getPositionList(selectedGridDate, activity)
        }


        return view

    }

    fun setNextMonth() {
        if (cal_month.get(GregorianCalendar.MONTH) == cal_month.getActualMaximum(GregorianCalendar.MONTH)) {
            cal_month.set(
                cal_month.get(GregorianCalendar.YEAR) + 1,
                cal_month.getActualMinimum(GregorianCalendar.MONTH),
                1
            )
        } else {
            cal_month.set(
                GregorianCalendar.MONTH,
                cal_month.get(GregorianCalendar.MONTH) + 1
            )
        }
    }

    fun setPreviousMonth() {
        if (cal_month.get(GregorianCalendar.MONTH) == cal_month.getActualMinimum(GregorianCalendar.MONTH)) {
            cal_month.set(
                cal_month.get(GregorianCalendar.YEAR) - 1,
                cal_month.getActualMaximum(GregorianCalendar.MONTH),
                1
            )
        } else {
            cal_month.set(GregorianCalendar.MONTH, cal_month.get(GregorianCalendar.MONTH) - 1)
        }
    }

    fun refreshCalendar() {
        hwAdapter?.refreshDays()
        hwAdapter?.notifyDataSetChanged()
        tv_month?.text = android.text.format.DateFormat.format("MMMM yyyy", cal_month)
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }
}