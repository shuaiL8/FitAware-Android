package com.vt.fitaware.Award


import android.app.Activity
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vt.fitaware.R
import android.widget.AdapterView
import android.widget.GridView
import androidx.navigation.Navigation
import com.google.firebase.database.*
import com.vt.fitaware.History.Histories
import com.vt.fitaware.History.HistoryAdapter
import com.vt.fitaware.Home.Teammates
import java.text.SimpleDateFormat
import java.util.*


class PersonalAwardFragment : Fragment() {

    private val TAG = "PersonalAwardFragment"


    private var awards = ArrayList<Award>(1)
    private lateinit var awardsAdapter: AwardAdapter

    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_personal_award, container,
            false)
        setHasOptionsMenu(true)
        initSharedPreferences()

        user_id = sharedPreferences!!.getString("user_id", "none")


        val gridViewPersonalAwards = view.findViewById<GridView>(R.id.gridViewPersonalAwards)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_awards_fragment).navigate(R.id.personalAwardFragment)
        }

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")
//        mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
        calendar.add(Calendar.DATE, -1)
        val strDate1 = mdformat.format(calendar.time)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_first)



        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                Log.i(TAG, "myTeamMember: $my")
                for((key, value) in my){
                    val details = value as Map<String, Map<String, String>>

                    if(details.containsKey(strDate1)){
                        val date = details.getValue(strDate1)

                        if(date!!.getValue("Rank") == "1"){
                            writeAwardsPost(strDate1, key, date!!.getValue("Steps"))
                        }
                    }
                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)


        val myRefAward = FirebaseDatabase.getInstance().reference.child("Award")

        val postListenerAward = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                awards.clear()

                for((key, value) in my){

                    val details = value as Map<String, String>

                    if(details["personal"] == user_id) {
                        awards.add(
                            Award(
                                bitmap,
                                details["personal"],
                                details["personalSteps"],
                                key,
                                "Best of Day"
                            )
                        )
                    }



                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")


                }
                Log.w(TAG, "awards$awards")
                val awardsSort = awards.sortedWith(compareByDescending(Award::getDate))
                awards = ArrayList(awardsSort)

                Log.w(TAG, "awards$awards")

                if (activity !=null){
                    awardsAdapter = AwardAdapter(
                        activity,
                        R.layout.awards,
                        awards
                    )
                    gridViewPersonalAwards.adapter = awardsAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRefAward.addValueEventListener(postListenerAward)




        gridViewPersonalAwards.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

        }

        return view
    }


    private fun writeAwardsPost(date: String, awardId: String, steps: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/Award/$date/personal"] = awardId
        childUpdates["/Award/$date/personalSteps"] = steps


        Log.w(TAG, "childUpdates Award: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }


}
