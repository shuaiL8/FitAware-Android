package com.vt.fitaware.History


import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.vt.fitaware.Communicator
import com.vt.fitaware.R
import com.google.firebase.database.*
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import androidx.navigation.Navigation
import com.vt.fitaware.Home.Teammates
import java.text.SimpleDateFormat
import java.util.*


class HistoryFragment : Fragment() {
    private val TAG = "HistoryFragment"

    private var histories = ArrayList<Histories>(1)
    private lateinit var historyAdapter: HistoryAdapter

    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"


    private var newSelectedDate: String = "none"

    private var allUsers = ArrayList<Teammates>(1)

    private var my_rank: String = "1"

    private var team: String = "none"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_history, container,
            false)
        initSharedPreferences()

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = "History"

        val bottomNavigationView = activity!!.findViewById<View>(R.id.bottomNavigation) as BottomNavigationView


        user_id = sharedPreferences!!.getString("user_id", "none")
        team = sharedPreferences!!.getString("team", "none")

//        my_rank = sharedPreferences!!.getString("my_rank", "1")

        val historyList = view.findViewById<ListView>(R.id.historyList)


//        val calendarNY = Calendar.getInstance()
//        val mdformatNY = SimpleDateFormat("yyyy-MM-dd")
////        mdformatNY.timeZone = TimeZone.getTimeZone("America/New_York")
//        val strDate = mdformatNY.format(calendarNY.time)

//        getRank()
//
//        recordDailyRank(
//            user_id,
//            strDate,
//            my_rank)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.historyFragment)

//            getRank()
//
//            recordDailyRank(
//                user_id,
//                strDate,
//                my_rank)
        }

        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                histories.clear()

                Log.i(TAG, "myTeamMember: $my")
                var index = 1
                for((key, value) in my){
                    if(key == user_id) {
                        val details = value as Map<String, Map<String, String>>

                        for((name, data) in details) {
                            val date = data

                            histories.add(
                                Histories(
                                    name,
                                    date.getValue("Rank"),
                                    date.getValue("Steps"),
                                    date.getValue("Goal"),
                                    date.getValue("Minis"),
                                    date.getValue("HPs"),
                                    date.getValue("Ms"),
                                    date.getValue("Cals")
                                )
                            )
                        }


                        index++
                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")
                    }


                }
                Log.w(TAG, "histories: $histories")
                val membersSort = histories.sortedWith(compareByDescending(Histories::getmDate))
                histories = ArrayList(membersSort)

                Log.w(TAG, "histories: $histories")

                if (activity !=null){
                    historyAdapter = HistoryAdapter(
                        activity,
                        R.layout.history_detail,
                        histories
                    )
                    historyList.adapter = historyAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)



        historyList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->

            if(team != "none") {
                Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.userFragment)


                newSelectedDate = histories[position].getmDate()

                val editor = sharedPreferences?.edit()
                editor!!.putString("newSelectedDate", newSelectedDate)

                editor.commit()
            }
            else{

                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder
                    .setMessage("First, you need to join in a team to see the team rank?")
                    .setPositiveButton("See Teams", DialogInterface.OnClickListener {
                            dialog, id ->
                        bottomNavigationView.selectedItemId = R.id.navigation_team
                        Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)

                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.show()
            }



        }


        return view
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }


    private fun initDaily(id: String, date: String, Cals: String, Goal: String, HPs: String, Minis: String, Ms: String, Steps: String, Token: String) {

        val childUpdates = java.util.HashMap<String, Any>()
        childUpdates["/DailyRecord/$id/$date/Cals"] = Cals
        childUpdates["/DailyRecord/$id/$date/Goal"] = Goal
        childUpdates["/DailyRecord/$id/$date/HPs"] = HPs
        childUpdates["/DailyRecord/$id/$date/Minis"] = Minis
        childUpdates["/DailyRecord/$id/$date/Ms"] = Ms
        childUpdates["/DailyRecord/$id/$date/Steps"] = Steps
        childUpdates["/DailyRecord/$id/$date/Token"] = Token

        Log.w(TAG, "initDaily childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

    }

    private fun recordDailyRank(id: String, date: String,Rank: String) {

        val childUpdates = java.util.HashMap<String, Any>()

        childUpdates["/DailyRecord/$id/$date/Rank"] = Rank

        Log.w(TAG, "recordDaily childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

    }

    fun getRank() {
        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")
//                    mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
        val strDate = mdformat.format(calendar.time)

        val myRefDailyRecord = FirebaseDatabase.getInstance().reference.child("DailyRecord")
        val postListenerDailyRecord = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                allUsers.clear()

                var index = 1
                for((key, value) in my){
                    val details = value as Map<String, Map<String, String>>

                    if(details.containsKey(strDate)) {
                        val dateMap = details.getValue(strDate)
                        allUsers.add(
                            Teammates(
                                "1",
                                key,
                                index.toString(),
                                dateMap.getValue("Steps").toInt(),
                                dateMap.getValue("Rank"),
                                dateMap.getValue("Minis").toInt(),
                                dateMap.getValue("HPs").toInt(),
                                dateMap.getValue("Ms").toInt(),
                                dateMap.getValue("Cals").toInt(),
                                "#3ebfab")
                        )
                        index++
                        Log.i(TAG, "dateMap: $key $dateMap")
                    }

                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")

                }

                // get overall_rank from all Users
                val allUsersSort = allUsers.sortedWith(compareByDescending(Teammates::getSteps))
                allUsers = ArrayList(allUsersSort)
                var indexM = 1
                for(users in allUsers) {
                    users.rank = indexM.toString()

                    if(users.name == user_id) {
                        my_rank = users.rank

                        val editor = sharedPreferences?.edit()
                        editor!!.putString("my_rank", my_rank)
                        editor.commit()

                        Log.i(TAG, "user_id: $user_id")
                        Log.i(TAG, "my_rank $my_rank")
                    }
                    indexM++
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRefDailyRecord.addValueEventListener(postListenerDailyRecord)
    }

}
