package com.vt.fitaware.History


import android.app.Activity
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
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.navigation.Navigation
import java.text.SimpleDateFormat
import java.util.*


class HistoryFragment : Fragment() {
    private val TAG = "HistoryFragment"

    private var histories = ArrayList<Histories>(1)
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var database: DatabaseReference

    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"
    private var my_goal: Long = 0
    private var my_rank: String = " "
    private var my_steps: Long = 0
    private var my_duration: Long = 0
    private var my_heartPoints: Long = 0
    private var my_calories: Long = 0
    private var my_distance: Long = 0

    private var token: String = "none"

    private var newSelectedDate: String = "none"


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

        user_id = sharedPreferences!!.getString("user_id", "none")
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        my_rank = sharedPreferences!!.getString("my_rank", " ")
        my_steps = sharedPreferences!!.getString("currentSteps", "0").toLong()
        my_duration = sharedPreferences!!.getString("duration", "0").toLong()
        my_heartPoints = sharedPreferences!!.getString("heartPoints", "0").toLong()
        my_distance = sharedPreferences!!.getString("distance", "0").toLong()
        my_calories = sharedPreferences!!.getString("calories", "0").toLong()
        token = sharedPreferences!!.getString("token", "none")


        val historyList = view.findViewById<ListView>(R.id.historyList)

        database = FirebaseDatabase.getInstance().reference

        val model = ViewModelProviders.of(activity!!).get(Communicator::class.java)
        val `object` = Observer<Any> { o ->
            // Update the UI

            Log.w(TAG, "allSteps" + o!!.toString())

            val value = o.toString().substring(1, o.toString().length - 1)
            val keyValuePairs = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val allSteps = java.util.HashMap<String, String>()

            for (pair in keyValuePairs) {
                val entry = pair.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                allSteps[entry[0].trim { it <= ' ' }] = entry[1].trim { it <= ' ' }
            }

            user_id = allSteps["user_id"]!!.toString()

            my_steps = allSteps["my_steps"]!!.toLong()
            my_heartPoints = allSteps["my_heartPoints"]!!.toLong()
            my_duration = allSteps["my_duration"]!!.toLong()
            my_distance = allSteps["my_distance"]!!.toLong()
            my_calories = allSteps["my_calories"]!!.toLong()

            my_goal = allSteps["my_goal"]!!.toLong()
            my_rank = allSteps["my_rank"]!!.toString()
        }

        model.message.observe(activity!!, `object`)


        val calendarNY = Calendar.getInstance()
        val mdformatNY = SimpleDateFormat("yyyy-MM-dd")
//        mdformatNY.timeZone = TimeZone.getTimeZone("America/New_York")
        val strDate = mdformatNY.format(calendarNY.time)


        val intent = Intent()
        intent.action = "com.vt.MyBackgroundServiceReceiver"
        intent.putExtra("my_rank", my_rank)
        intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        activity!!.sendBroadcast(intent)


        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.historyFragment)

            initDaily(
                user_id,
                strDate,
                my_calories.toString(),
                my_goal.toString(),
                my_heartPoints.toString(),
                my_duration.toString(),
                my_distance.toString(),
                my_rank,
                my_steps.toString(),
                token)

            val intent = Intent()
            intent.action = "com.vt.MyBackgroundServiceReceiver"
            intent.putExtra("my_rank", my_rank)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            activity!!.sendBroadcast(intent)
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

            Navigation.findNavController(activity!!, R.id.my_nav_host_fragment).navigate(R.id.userFragment)


            newSelectedDate = histories[position].getmDate()

            val editor = sharedPreferences?.edit()
            editor!!.putString("newSelectedDate", newSelectedDate)

            editor.commit()

        }


        return view
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }


    private fun initDaily(id: String, date: String, Cals: String, Goal: String, HPs: String, Minis: String, Ms: String, Rank: String, Steps: String, Token: String) {

        val childUpdates = java.util.HashMap<String, Any>()
        childUpdates["/DailyRecord/$id/$date/Cals"] = Cals
        childUpdates["/DailyRecord/$id/$date/Goal"] = Goal
        childUpdates["/DailyRecord/$id/$date/HPs"] = HPs
        childUpdates["/DailyRecord/$id/$date/Minis"] = Minis
        childUpdates["/DailyRecord/$id/$date/Ms"] = Ms
        childUpdates["/DailyRecord/$id/$date/Rank"] = Rank
        childUpdates["/DailyRecord/$id/$date/Steps"] = Steps
        childUpdates["/DailyRecord/$id/$date/Token"] = Token

        Log.w(TAG, "initDaily childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

    }

}
