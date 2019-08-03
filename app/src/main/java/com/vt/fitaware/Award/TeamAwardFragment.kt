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
import android.widget.AdapterView
import android.widget.GridView
import androidx.navigation.Navigation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vt.fitaware.Home.Teammates
import com.vt.fitaware.R
import java.text.SimpleDateFormat
import java.util.*


class TeamAwardFragment : Fragment() {

    private val TAG = "TeamAwardFragment"


    private var awards = ArrayList<Award>(1)
    private lateinit var awardsAdapter: AwardAdapter

    private var teammates = ArrayList<Teammates>(1)


    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"
    private var team: String = "none"
    private var teamSteps: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            R.layout.fragment_team_award, container,
            false)
        setHasOptionsMenu(true)
        initSharedPreferences()

        user_id = sharedPreferences!!.getString("user_id", "none")
        team = sharedPreferences!!.getString("team", "none")

        val gridViewTeamAwards = view.findViewById<GridView>(R.id.gridViewTeamAwards)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_awards_fragment).navigate(R.id.teamAwardFragment)
        }
        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")
        mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
        calendar.add(Calendar.DATE, -1)
        val strDate1 = mdformat.format(calendar.time)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_trophy)


        val myRefTeams = FirebaseDatabase.getInstance().reference.child("Teams")

        val postListenerTeams = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.value != null){
                    val myTeams = dataSnapshot.value as Map<String, Any>




                    for((keyTeams, valueTeams) in myTeams){
                        val detailsTeams = valueTeams as Map<String, String>

                        Log.i(TAG, "detailsTeams: $detailsTeams")


                        val teamMembers = detailsTeams.getValue("teamMembers") as Map<String, String>

                        Log.i(TAG, "teamMembers: $teamMembers")

                        var iniTeamSteps = 0
                        teammates.clear()


                        for((mem, valueMem) in teamMembers) {

                            val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord")

                            val postListener = object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    // Get Post object and use the values to update the UI
                                    val my = dataSnapshot.value as Map<String, Any>

                                    if(my.containsKey(mem)) {

                                        Log.i(TAG, "myTeamMember mem: $mem")


                                        val details = my.getValue(mem) as Map<String, Map<String, String>>

                                        if(details.containsKey(strDate1)){
                                            val date = details.getValue(strDate1)

                                            iniTeamSteps += date.getValue("Steps").toInt()

                                            Log.i(TAG, "iniTeamSteps: $iniTeamSteps")

                                        }

                                    }

                                    teamSteps = iniTeamSteps
                                    Log.i(TAG, "teamSteps: $teamSteps")

                                    teammates.add(Teammates("0",  keyTeams, "0", teamSteps, "0", 0, 0, 0, 0, "#ff6347"))

                                    val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getSteps))
                                    teammates = ArrayList(teammatesSort)
                                    writeAwardsPost(strDate1, teammates[0].name, teammates[0].steps.toString())
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Getting Post failed, log a message
                                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                                    // ...
                                }
                            }
                            myRef.addValueEventListener(postListener)

                        }

                        Log.i(TAG, "$keyTeams: $valueTeams")
                        Log.i(TAG, "detailsTeams: $detailsTeams")
                    }





                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRefTeams.addValueEventListener(postListenerTeams)






        val myRefAward = FirebaseDatabase.getInstance().reference.child("Award")

        val postListenerAward = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                awards.clear()

                for((key, value) in my){

                    val details = value as Map<String, String>

                    if(details["team"] == team) {
                        awards.add(
                            Award(
                                bitmap,
                                details["team"],
                                details["teamSteps"],
                                key,
                                "Best Team of Day"
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
                    gridViewTeamAwards.adapter = awardsAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRefAward.addValueEventListener(postListenerAward)


        gridViewTeamAwards.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

        }

        return view
    }

    private fun writeAwardsPost(date: String, awardId: String, steps: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/Award/$date/team"] = awardId
        childUpdates["/Award/$date/teamSteps"] = steps

        Log.w(TAG, "childUpdates Award: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

}
