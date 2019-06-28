package com.vt

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vt.fitaware.Home.Teammates
import com.vt.fitaware.MainActivity
import com.vt.fitaware.R
import com.vt.fitaware.Team.Team
import java.util.*

class MyNotificationService : Service() {

    private val TAG = "MyNotificationService"

    private var sharedPreferences: SharedPreferences? = null

    private var mTimer: Timer? = null

    private var my_steps: Long = 0
    private var my_rank: String = "0"

    private var team: String = "none"

    private var team_steps: Long = 0
    private var teamRank: String = "0"
    private var teams = ArrayList<Team>(1)
    private var teammates = ArrayList<Teammates>(1)
    private var user_id: String = "none"
    private var loginStatus = 1

    private var teammemberCount: Int = 0
    private var teamCount: Int = 0

    override fun onBind(intent: Intent): IBinder {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        user_id = sharedPreferences!!.getString("user_id", "none")
        loginStatus = sharedPreferences!!.getInt("loginStatus", 0)
        if(loginStatus == 1) {
            val myRef = FirebaseDatabase.getInstance().reference.child("User")
            val myPostListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>
                    Log.i(TAG, "user: $my")

                    var iniTeamSteps = 0L
                    var index = 1

                    teammates.clear()

                    for ((key, value) in my) {
                        val details = value as Map<String, String>


                        if (key == user_id) {

                            my_steps = details["currentSteps"].toString().toLong()
                            team = details["team"].toString()

                            teammates.add(Teammates("1", key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(),"#008577"))


                            iniTeamSteps += my_steps

                        }
                        if(details.containsKey("team")) {
                            if(details["team"].toString() != "none") {
                                if(details.getValue("team") == team && key != user_id){
                                    teammates.add(Teammates("1",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#3ebfab"))
                                    iniTeamSteps += details["currentSteps"].toString().toLong()
                                }

                            }
                        }

                        index++

                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    team_steps = iniTeamSteps




                    val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getSteps))
                    teammates = ArrayList(teammatesSort)
                    var indexM = 1
                    for(teammate in teammates) {
                        teammate.rank = indexM.toString()

                        if(teammate.name == user_id) {
                            my_rank = teammate.rank
                            val editor = sharedPreferences?.edit()
                            editor!!.putString("rank", my_rank)

                            editor.commit()

                            Log.i(TAG, "user_id $user_id")
                            Log.i(TAG, "my_rank $my_rank")
                        }
                        indexM++
                    }
                    teammemberCount = indexM-1
                    Log.w(TAG, "teammemberCount"+ teammemberCount)


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            myRef.addValueEventListener(myPostListener)


            val myRefTeam = FirebaseDatabase.getInstance().reference.child("Teams")

            val postListenerTeam = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if(dataSnapshot.value != null){
                        val my = dataSnapshot.value as Map<String, Any>

                        Log.i(TAG, "myTeamMember: $my")

                        teams.clear()

                        for((key, value) in my){
                            val details = value as Map<String, String>

                            teams.add(
                                Team(
                                    key,
                                    details.getValue("captain"),
                                    "No. ?",
                                    details.getValue("teamGoal"),
                                    details.getValue("teamSteps").toInt(),
                                    details.getValue("periodical")
                                )
                            )

                            Log.i(TAG, "$key: $value")
                            Log.i(TAG, "details: $details")
                        }

                        val teammsSort = teams.sortedWith(compareByDescending(Team::getTeamSteps))
                        teams = ArrayList(teammsSort)
                        var indexX = 1
                        for(teamX in teams) {

                            teamX.rank = indexX.toString()
                            if(teamX.name == team) {
                                teamRank = teamX.rank

                                val editor = sharedPreferences?.edit()
                                editor!!.putString("teamRank", teamRank)

                                editor.commit()

                                Log.i(TAG, "teamRank: $teamRank")

                            }
                            indexX++
                        }
                        teamCount = indexX-1
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            myRefTeam.addValueEventListener(postListenerTeam)

        }



        //fire notification on notification drawer
        fireNotification()
    }

    private fun fireNotification(){
        //get Date and sync data
        mTimer = Timer()
        val delay = 3000 // delay for 0 sec.
        val period = 600000 // repeat 10 mins.

        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                showNotification()
            }
        }, delay.toLong(), period.toLong())
    }


    fun showNotification() {

        val collapsedView = RemoteViews(packageName, R.layout.notif_collapsed)
        collapsedView.setTextViewText(R.id.textMySteps, my_steps.toString())
        collapsedView.setTextViewText(R.id.textMyRank, "$my_rank/$teammemberCount")
        collapsedView.setTextViewText(R.id.textTeamSteps, team_steps.toString())
        collapsedView.setTextViewText(R.id.textTeamRank, "$teamRank/$teamCount")

        val expandedView = RemoteViews(packageName, R.layout.notif_expanded)
        expandedView.setTextViewText(R.id.textMySteps, my_steps.toString())
        expandedView.setTextViewText(R.id.textMyRank, "$my_rank/$teammemberCount")
        expandedView.setTextViewText(R.id.textTeamSteps, team_steps.toString())
        expandedView.setTextViewText(R.id.textTeamRank, "$teamRank/$teamCount")

        val builder = NotificationCompat.Builder(this, "Notification Drawer")
            // these are the three things a NotificationCompat.Builder object requires at a minimum
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle("FitAware")
            .setContentText("")
            .setVibrate(null)
            // notification will be dismissed when tapped
            .setAutoCancel(false).setOngoing(true)
            // tapping notification will open MainActivity
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0))
            // setting the custom collapsed and expanded views
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
//            .setDeleteIntent(createOnDismissedIntent(this, 3714))
            // setting style to DecoratedCustomViewStyle() is necessary for custom views to display
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        // retrieves android.app.NotificationManager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(0, builder.build())

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "FitAware",
                "Notification Drawer",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Always on"
            notificationManager!!.createNotificationChannel(channel)
        }
        notificationManager!!.notify(0, builder.build())
        Log.d("socketio", "notification issued")
//        trackWatchEvent("Android Notification Shown")

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "Service Stopped.")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancelAll()
    }

}
