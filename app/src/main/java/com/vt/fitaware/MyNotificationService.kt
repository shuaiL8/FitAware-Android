package com.vt.fitaware

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vt.fitaware.Home.Teammates
import com.vt.fitaware.Team.Team
import java.util.*
import java.util.concurrent.TimeUnit

class MyNotificationService : Service() {

    private val TAG = "MyNotificationService"

    private var mTimer: Timer? = null

    private var my_steps: Long = 0
    private var my_rank_inTeam: String = "0"

    private var team: String = "none"

    private var team_steps: Long = 0
    private var teamRank: String = "0"
    private var teams = ArrayList<Team>(1)
    private var teammates = ArrayList<Teammates>(1)
    private var user_id: String = "none"

    private var teammemberCount: Int = 0
    private var teamCount: Int = 0

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented") as Throwable
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.i(TAG, "Service started.")

        Log.d(TAG,"user_id " + intent.extras.getString("user_id"))
        Log.d(TAG,"team " + intent.extras.getString("team"))

        user_id = intent.extras.getString("user_id")
        team = intent.extras.getString("team")

        showNotification()

        runNotification()

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "service oncreate")

        getFireBaseData()

    }

    fun getFireBaseData() {
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
                    else {

                        if(details["team"].toString() == team && details["team"].toString() != "none"){
                            teammates.add(Teammates("1",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#3ebfab"))
                            iniTeamSteps += details["currentSteps"].toString().toLong()
                        }

                    }

                    index++

                    Log.i(TAG, "$key: $value")
                    Log.i(TAG, "details: $details")

                }

                Log.w(TAG, "teamNotNone"+ team)


                if(team != "none") {
                    team_steps = iniTeamSteps

                    val teammatesSort = teammates.sortedWith(compareByDescending(Teammates::getSteps))
                    teammates = ArrayList(teammatesSort)
                    var indexM = 1
                    for(teammate in teammates) {
                        teammate.rank = indexM.toString()

                        if(teammate.name == user_id) {
                            my_rank_inTeam = teammate.rank

                            Log.i(TAG, "user_id $user_id")
                            Log.i(TAG, "my_rank $my_rank_inTeam")
                        }
                        indexM++
                        Log.w(TAG, "indexM counts"+ indexM)

                    }
                    teammemberCount = teammates.size
                    Log.w(TAG, "teammemberCount"+ teammemberCount)


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
                                            details.getValue("periodical"),
                                            "#ff6347"
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

                                        Log.i(TAG, "teamRank: $teamRank")

                                    }
                                    indexX++
                                }
                                teamCount = teams.size
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
                //team != none end

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(myPostListener)
    }

    private fun runNotification() {

        Thread(Runnable {

            //Run Notification
            mTimer = Timer()
            val delay = 20000 // delay for 20 sec.
            val period = 600000 // repeat 10 mins.

            mTimer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {

                    showNotification()

                    Log.d(TAG, "test service my_steps $my_steps")
                    Log.d(TAG, "test service my_rank $my_rank_inTeam/$teammemberCount")
                    Log.d(TAG, "test service team_steps $team_steps")
                    Log.d(TAG, "test service teamRank $teamRank/$teamCount")

                }
            }, delay.toLong(), period.toLong())

        }).start()

    }


    fun showNotification() {


        val collapsedView = RemoteViews(packageName, R.layout.notif_collapsed)
        collapsedView.setTextViewText(R.id.textMySteps, my_steps.toString())
        collapsedView.setTextViewText(R.id.textMyRank, "$my_rank_inTeam/$teammemberCount")
        collapsedView.setTextViewText(R.id.textTeamSteps, team_steps.toString())
        collapsedView.setTextViewText(R.id.textTeamRank, "$teamRank/$teamCount")

        val expandedView = RemoteViews(packageName, R.layout.notif_expanded)
        expandedView.setTextViewText(R.id.textMySteps, my_steps.toString())
        expandedView.setTextViewText(R.id.textMyRank, "$my_rank_inTeam/$teammemberCount")
        expandedView.setTextViewText(R.id.textTeamSteps, team_steps.toString())
        expandedView.setTextViewText(R.id.textTeamRank, "$teamRank/$teamCount")


        val CHANNEL_ID = "Notification Drawer"

        if(Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Drawer",
                NotificationManager.IMPORTANCE_LOW
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle("FitAware")
            .setContentText("")
            .setVibrate(longArrayOf(0))
            .setAutoCancel(false).setOngoing(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0))
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()

        if (Build.VERSION.SDK_INT >= 26) {

            startForeground(1, notification)
        }
        else{

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notification)
        }


    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "Service Stopped.")

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

}
