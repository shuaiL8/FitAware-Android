package com.example.fitaware.Team


import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import com.example.fitaware.Communicator
import com.example.fitaware.History.Histories
import com.example.fitaware.R
import com.google.firebase.database.*
import java.util.HashMap
import android.arch.lifecycle.Observer
import android.content.SharedPreferences
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import androidx.navigation.Navigation
import com.example.fitaware.Home.Teammates


class AllTeamsFragment : Fragment() {

    private val TAG = "AllTeamsFragment"
    private var teams = ArrayList<Team>(1)
    private lateinit var teamsAdapter: TeamAdapter
    private var user_id: String = ""
    private var captain: String = ""
    private lateinit var database: DatabaseReference
    private var sharedPreferences: SharedPreferences? = null
    private var teamGoal: String = "0"
    private var periodical: String = ""
    private var selectedName: String = ""
    private var tabLayoutPeriodical: String = ""
    private var oldTeamName: String = "none"
    private var oldTeamCaptain: String = "none"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(
            R.layout.fragment_all_teams, container,
            false)
        setHasOptionsMenu(false)
        initSharedPreferences()

        tabLayoutPeriodical = sharedPreferences!!.getString("tabLayoutPeriodical", "0")

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
            oldTeamName = allSteps["team"]!!.toString()
            oldTeamCaptain = allSteps["captain"]!!.toString()
        }

        model.message.observe(activity!!, `object`)

        val gridViewAllTeams = view.findViewById<GridView>(R.id.gridViewAllTeams)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        swipeRefresh.setOnRefreshListener {
            Navigation.findNavController(context as Activity, R.id.my_nav_team_fragment).navigate(R.id.allTeamsFragment)
        }

        teams.clear()

        val myRef = FirebaseDatabase.getInstance().reference.child("Teams")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                if(dataSnapshot.value != null){
                    val my = dataSnapshot.value as Map<String, Any>

                    Log.i(TAG, "myTeamMember: $my")

                    teams.clear()

                    for((key, value) in my){
                        val details = value as Map<String, String>

                        if(tabLayoutPeriodical.toLowerCase() == details.getValue("periodical").toString().toLowerCase()) {

                            var bitmap = BitmapFactory.decodeResource(resources, R.drawable.teamwork)

                            teams.add(
                                Team(
                                    bitmap,
                                    key,
                                    details.getValue("captain"),
                                    "No. ?",
                                    details.getValue("teamGoal"),
                                    details.getValue("teamSteps").toInt(),
                                    details.getValue("periodical")
                                )
                            )
                        }
                        else if(tabLayoutPeriodical.toLowerCase() == "all") {
                            var bitmap = BitmapFactory.decodeResource(resources, R.drawable.teamwork)

                            teams.add(
                                Team(
                                    bitmap,
                                    key,
                                    details.getValue("captain"),
                                    "No. ?",
                                    details.getValue("teamGoal"),
                                    details.getValue("teamSteps").toInt(),
                                    details.getValue("periodical")
                                )
                            )
                        }


                        Log.i(TAG, "tabLayoutPeriodical: ${tabLayoutPeriodical.toLowerCase()}")
                        Log.i(TAG, "periodical: "+details.getValue("periodical").toString().toLowerCase())

                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")
                    }

                    val teammsSort = teams.sortedWith(compareByDescending(Team::getTeamSteps))
                    teams = java.util.ArrayList(teammsSort)
                    var indexM = 1
                    for(team in teams) {
                        team.rank = indexM.toString()
                        indexM++
                    }

                    teamsAdapter = TeamAdapter(
                        activity,
                        R.layout.teams,
                        teams
                    )
                    gridViewAllTeams.adapter = teamsAdapter

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)



        gridViewAllTeams.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            selectedName = teams[position].name
            teamGoal = teams[position].goal
            periodical = teams[position].periodical
            captain = teams[position].captain

            if(captain == user_id) {
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder
                    .setMessage("You are the captain of $selectedName")
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.show()
            }
            else if(oldTeamName == selectedName) {
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder
                    .setMessage("You are already in the Team: $selectedName")
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                alert.show()
            }
            else {
                val dialogBuilder = AlertDialog.Builder(context)
                dialogBuilder
                    .setMessage("Do you want to join in $selectedName?")
                    .setPositiveButton("Join in", DialogInterface.OnClickListener {
                            dialog, id ->
                        val editor = sharedPreferences?.edit()
                        editor!!.putString("team", selectedName)
                        editor!!.putString("team_goal", teamGoal)

                        editor.commit()

                        writeNewPost(user_id, selectedName, captain,  teamGoal, periodical)
                        showSnackBarMessage("You joined in $selectedName!")
                        Navigation.findNavController(context as Activity, R.id.my_nav_team_fragment).navigate(R.id.allTeamsFragment)

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

    private fun writeNewPost(id: String, team: String, captain: String, teamG: String, teamPeriodical: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/User/$id/team"] = team
        childUpdates["/User/$id/captain"] = captain
        childUpdates["/User/$id/teamGoal"] = teamG
        childUpdates["/User/$id/periodical"] = teamPeriodical

        if(oldTeamName != "none") {
            database.child("/Teams/$oldTeamName/teamMembers/$id").removeValue()
            if (oldTeamCaptain == id) {
                childUpdates["/Teams/$oldTeamName/captain"] = "none"
            }

        }


        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)

        database.child("/Teams/$team/teamMembers/$id").setValue(id)
    }

    private fun showSnackBarMessage(message: String) {

        if (view != null) {

            Snackbar.make(view!!, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

}