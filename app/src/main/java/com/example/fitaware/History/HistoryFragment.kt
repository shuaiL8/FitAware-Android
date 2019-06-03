package com.example.fitaware.History


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
import com.example.fitaware.Communicator
import com.example.fitaware.R
import com.google.firebase.database.*
import android.arch.lifecycle.Observer
import androidx.navigation.Navigation
import java.text.SimpleDateFormat
import java.util.*


class HistoryFragment : Fragment() {
    private val TAG = "HistoryFragment"

    private var histories = ArrayList<Histories>(1)
    private lateinit var historyAdapter: HistoryAdapter
    private var user_id: String = ""

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_history, container,
            false)

        val toolbarTiltle = activity!!.findViewById<TextView>(R.id.toolbar_title)
        toolbarTiltle.text = "History"

        val historyList = view.findViewById<ListView>(R.id.historyList)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_host_fragment).navigate(R.id.historyFragment)
        }

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
        }

        model.message.observe(activity!!, `object`)

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
                Log.w(TAG, "histories" + histories.toString())
                val membersSort = histories.sortedWith(compareByDescending(Histories::getmDate))
                histories = ArrayList(membersSort)

                Log.w(TAG, "histories" + histories.toString())

                historyAdapter = HistoryAdapter(
                    activity,
                    R.layout.history_detail,
                    histories
                )
                historyList.adapter = historyAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)



        historyList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->


        }


        return view
    }



}
