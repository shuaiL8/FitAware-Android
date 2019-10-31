package com.vt.fitaware

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.arch.lifecycle.ViewModelProviders
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import androidx.navigation.Navigation
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.vt.fitaware.FirebaseMessagingService.MyReceiver
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataSourcesRequest
import com.google.android.gms.fitness.result.DataSourcesResult
import com.google.android.gms.location.places.Places
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.vt.fitaware.Home.Teammates
import com.vt.fitaware.Team.Team
import com.vt.fitaware.Team.TeamAdapter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {



    private val TAG = "MainActivity"
    private lateinit var database: DatabaseReference
    private var mClient: GoogleApiClient? = null
    private var receiver: BroadcastReceiver = MyReceiver()

    private var authInProgress = false
    private var REQUEST_OAUTH = 1

    private var loginStatus = 1

    private var allUsers = ArrayList<Teammates>(1)


    private var captain: String = "none"

    private var teamRank: String = "0"
    private var teams = ArrayList<Team>(1)

    private var user_id: String = "none"

    private var periodical: String = "none"
    private var team: String = "none"


    private var my_steps: Long = 0
    private var my_duration: Long = 0
    private var my_heartPoints: Long = 0
    private var my_calories: Long = 0
    private var my_distance: Long = 0

    private var sharedPreferences: SharedPreferences? = null

    private var teammate_steps: Long = 0
    private var team_steps: Long = 0

    private var my_rank: String = " "
    private var my_goal: Long = 0
    private var teammate_goal: Long = 0
    private var team_goal: Long = 0


    private var teammemberCount: Int = 0
    private var teamCount: Int = 0


    private var model: Communicator?=null
    private var mTimer: Timer? = null
    private var bottomNavigationView: BottomNavigationView? = null
    private val RECORD_REQUEST_CODE = 101

    private var token = "none"

    private var daily_steps: Long = 0
    private var daily_heartPoints: Long = 0
    private var daily_duration: Long = 0
    private var daily_calories: Long = 0
    private var daily_distance: Long = 0

    private var threeDays_steps: Long = 0
    private var threeDays_distance: Long = 0
    private var threeDays_calories: Long = 0

    private var fiveDays_steps: Long = 0
    private var fiveDays_distance: Long = 0
    private var fiveDays_calories: Long = 0

    private var weekly_steps: Long = 0
    private var weekly_distance: Long = 0
    private var weekly_calories: Long = 0

    private var myNotificationServiceStatus: String = "startMyNotificationService"


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

            R.id.navigation_home -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "FitAware"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.homeFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_history -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Me"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.historyFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_team -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Team"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.teamFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_awards-> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Awards"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.awardsFragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
//                val actionBar = supportActionBar
//                actionBar?.hide()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = ""

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.settingFragment)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    private fun setupPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE)
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RECORD_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user")
                } else {
                    Log.i(TAG, "Permission has been granted by user")
                }
            }
        }
    }

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSharedPreferences()
        setupPermissions()

        mClient = GoogleApiClient.Builder(this)
            .addApi(Fitness.SENSORS_API)
            .addApi(Fitness.RECORDING_API)
            .addApi(Fitness.HISTORY_API)
            .addApi(Fitness.SESSIONS_API)
            .addApi(Fitness.CONFIG_API)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .addScope(Scope(Scopes.FITNESS_ACTIVITY_READ))
            .addScope(Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
            .addScope(Scope(Scopes.FITNESS_LOCATION_READ))
            .enableAutoManage(this, 0, this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()


        // initial all the values
        user_id = sharedPreferences!!.getString("user_id", "none")
        team = sharedPreferences!!.getString("team", "none")
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        my_rank = sharedPreferences!!.getString("my_rank", " ")
        team_goal = sharedPreferences!!.getString("team_goal", "0").toLong()
        captain = sharedPreferences!!.getString("captain", "none")
        periodical = sharedPreferences!!.getString("periodical", "none")
        myNotificationServiceStatus = sharedPreferences!!.getString("MyNotificationServiceStatus", "startMyNotificationService")

        loginStatus = sharedPreferences!!.getInt("loginStatus", 0)
        Log.i(TAG, "teamName: $team")

        Log.i(TAG, "sharedPreferences my_goal: $my_goal")
        Log.i(TAG, "user_id: $user_id")
        Log.i(TAG, "loginStatus: $loginStatus")

        bottomNavigationView = findViewById<View>(R.id.bottomNavigation) as BottomNavigationView
        bottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar!!.title = ""

        // Firebase notification token
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                token = task.result?.token.toString()

                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)

                val editor = sharedPreferences?.edit()
                editor!!.putString("token", token)

                editor!!.commit()

            // Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
            })

        if(loginStatus == 1 && user_id != "none") {

            val myBackgroundWorker = PeriodicWorkRequest.Builder(
                MyBackgroundWorker::class.java,
                15,
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance().enqueue(myBackgroundWorker)

            val fitnessDataTask = FitnessDataTask()
            fitnessDataTask.execute()

            //get date from google api
            getData()


            val myRefUser = FirebaseDatabase.getInstance().reference.child("User")
            val myPostListenerUser = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>
                    Log.i(TAG, "user: $my")

                    var iniTeamSteps = 0L
                    var index = 1

                    for ((key, value) in my) {
                        val details = value as Map<String, String>

                        if (key == user_id) {
                            captain = details["captain"].toString()
                            if (captain == user_id) {
                                bottomNavigationView!!.menu.getItem(0).title = "Captain"

                                bottomNavigationView!!.menu.getItem(0).setIcon(R.drawable.ic_captain_america_shield)
                            }

                            my_steps = details["currentSteps"].toString().toLong()
                            my_heartPoints = details["heartPoints"].toString().toLong()
                            my_duration = details["duration"].toString().toLong()
                            my_distance = details["distance"].toString().toLong()
                            my_calories = details["calories"].toString().toLong()

                            my_goal = details["goal"].toString().toLong()
                            periodical = details["periodical"].toString()
                            team = details["team"].toString()

                            iniTeamSteps += my_steps

                        }
                        else {

                            if(details["team"].toString() == team && details["team"].toString() != "none"){
                                iniTeamSteps += details["currentSteps"].toString().toLong()
                            }

                        }



                        index++

                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details teammates: $details")

                    }


                    if (team != "none") {

                        team_steps = iniTeamSteps

                        writeTeamStepsPost(user_id, team, team_steps.toString())

                        val editor = sharedPreferences?.edit()
                        editor!!.putString("teamSteps", team_steps.toString())

                        editor.commit()

                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            myRefUser.addValueEventListener(myPostListenerUser)


            if(team != "none") {
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

                                if(key == team){
                                    team_goal = details.getValue("teamGoal").toLong()
                                    periodical = details.getValue("periodical")

                                }

                                Log.i(TAG, "$key: $value")
                                Log.i(TAG, "details: $details")
                            }

                            if(user_id != "none") {
                                val childUpdates = HashMap<String, Any>()
                                childUpdates["/User/$user_id/teamGoal"] = team_goal.toString()
                                childUpdates["/User/$user_id/periodical"] = periodical

                                database.updateChildren(childUpdates)

                                val editor = sharedPreferences?.edit()
                                editor!!.putString("periodical", periodical)

                                editor!!.commit()
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


            val myRefDailyRecord = FirebaseDatabase.getInstance().reference.child("DailyRecord")

            val postListenerDailyRecord = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>

                    allUsers.clear()

                    val calendar = Calendar.getInstance()
                    val mdformat = SimpleDateFormat("yyyy-MM-dd")
//                    mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
                    val strDate = mdformat.format(calendar.time)

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
                                    "#3ebfab"))
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


            val allSteps = HashMap<String, Any>()

            allSteps["user_id"] = user_id
            allSteps["periodical"] = periodical
            allSteps["team"] = team
            allSteps["captain"] = captain
            allSteps["teamRank"] = teamRank

            allSteps["teammate_steps"] = teammate_steps
            allSteps["my_steps"] = my_steps
            allSteps["my_heartPoints"] = my_heartPoints
            allSteps["my_duration"] = my_duration
            allSteps["my_distance"] = my_distance
            allSteps["my_calories"] = my_calories

            allSteps["team_steps"] = team_steps

            allSteps["my_goal"] = my_goal
            allSteps["my_rank"] = my_rank
            allSteps["teammate_goal"] = teammate_goal
            allSteps["team_goal"] = team_goal



            model= ViewModelProviders.of(this).get(Communicator::class.java)
            model!!.setMsgCommunicator(allSteps.toString())
            Log.w(TAG, "allSteps$allSteps")
        }

        if (loginStatus == 1) {
            bottomNavigationView!!.visibility = View.VISIBLE
            Navigation.findNavController(this@MainActivity, R.id.my_nav_host_fragment).navigate(R.id.homeFragment)

        } else {
            bottomNavigationView!!.visibility = View.GONE
            Navigation.findNavController(this@MainActivity, R.id.my_nav_host_fragment).navigate(R.id.loginFragment)
        }

        database = FirebaseDatabase.getInstance().reference

        Log.w(TAG, "user_id: "+user_id)

    }

    private inner class FitnessDataTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            //get Date from Google Fit
            mTimer = Timer()
            val delay = 0 // delay for 0 sec.
            val period = 30000 // repeat 30 sec.

            mTimer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {

                    var totalDuration: Long = 0

                    val resultDuration = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_MOVE_MINUTES)
                    val totalResultDuration = resultDuration.await(30, TimeUnit.SECONDS)
                    if (totalResultDuration.status.isSuccess) {
                        val totalSetDuration = totalResultDuration.total
                        totalDuration = (if (totalSetDuration!!.isEmpty)
                            0
                        else
                            totalSetDuration.dataPoints[0].getValue(Field.FIELD_DURATION).asInt()).toLong()
                    } else {
                        Log.w(TAG, "There was a problem getting the duration.")
                    }

                    Log.i(TAG, "Total duration: $totalDuration")

                    daily_duration = totalDuration


                    var totalSteps: Long = 0

                    val resultSteps = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                    val totalResultSteps = resultSteps.await(30, TimeUnit.SECONDS)
                    if (totalResultSteps.status.isSuccess) {
                        val totalSetSteps = totalResultSteps.total
                        totalSteps = (if (totalSetSteps!!.isEmpty)
                            0
                        else
                            totalSetSteps.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()).toLong()
                    } else {
                        Log.w(TAG, "There was a problem getting the step count.")
                    }

                    Log.i(TAG, "Total steps: $totalSteps")

                    daily_steps = totalSteps


                    var totalHeartPoints: Long = 0

                    val resultHeartPoints = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_HEART_POINTS)
                    val totalResultHeartPoints = resultHeartPoints.await(30, TimeUnit.SECONDS)
                    if (totalResultHeartPoints.status.isSuccess) {
                        val totalSetHeartPoints = totalResultHeartPoints.total
                        totalHeartPoints = (if (totalSetHeartPoints!!.isEmpty)
                            0
                        else
                            "%.0f".format(totalSetHeartPoints.dataPoints[0].getValue(Field.FIELD_INTENSITY).asFloat())).toString().toLong()

                    } else {
                        Log.w(TAG, "There was a problem getting the HeartPoints.")
                    }

                    Log.i(TAG, "Total HeartPoints: $totalHeartPoints")

                    daily_heartPoints = totalHeartPoints


                    var totalDistance: Long = 0

                    val resultDistance = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_DISTANCE_DELTA)
                    val totalResultDistance = resultDistance.await(30, TimeUnit.SECONDS)
                    if (totalResultDistance.status.isSuccess) {
                        val totalSetDistance = totalResultDistance.total
                        totalDistance = (if (totalSetDistance!!.isEmpty)
                            0
                        else
                            "%.0f".format(totalSetDistance.dataPoints[0].getValue(Field.FIELD_DISTANCE).asFloat())).toString().toLong()
                    } else {
                        Log.w(TAG, "There was a problem getting the Distance.")
                    }

                    Log.i(TAG, "Total Distance: $totalDistance")

                    daily_distance = totalDistance


                    var totalCalories: Long = 0

                    val resultCalories = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_CALORIES_EXPENDED)
                    val totalResultCalories = resultCalories.await(30, TimeUnit.SECONDS)
                    if (totalResultCalories.status.isSuccess) {
                        val totalSetCalories = totalResultCalories.total
                        totalCalories = (if (totalSetCalories!!.isEmpty)
                            0
                        else
                            "%.0f".format(totalSetCalories.dataPoints[0].getValue(Field.FIELD_CALORIES).asFloat())).toString().toLong()
                    } else {
                        Log.w(TAG, "There was a problem getting the Calories.")
                    }

                    Log.i(TAG, "Total Calories: $totalCalories")

                    daily_calories = totalCalories


                    val calendar = Calendar.getInstance()
                    val mdformat = SimpleDateFormat("yyyy-MM-dd")
//                    mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
                    val strDate = mdformat.format(calendar.time)

                    recordDaily(
                        user_id,
                        strDate,
                        daily_calories.toString(),
                        my_goal.toString(),
                        daily_heartPoints.toString(),
                        daily_duration.toString(),
                        daily_distance.toString(),
                        my_rank,
                        daily_steps.toString(),
                        token)

                    when {
                        periodical.toLowerCase() == "daily" -> {

                            writeNewPost(
                                user_id,
                                daily_steps.toString(),
                                daily_duration.toString(),
                                daily_heartPoints.toString(),
                                daily_distance.toString(),
                                daily_calories.toString()
                            )

                        }

                        periodical == "3 days" -> {

                            threeDaysRecord()

                            writeNewPost(
                                user_id,
                                threeDays_steps.toString(),
                                daily_duration.toString(),
                                daily_heartPoints.toString(),
                                threeDays_distance.toString(),
                                threeDays_calories.toString()
                            )


                            Log.i(TAG, "threeDays_steps: $threeDays_steps")

                        }
                        periodical == "5 days" -> {

                            fiveDaysRecord()

                            writeNewPost(
                                user_id,
                                fiveDays_steps.toString(),
                                daily_duration.toString(),
                                daily_heartPoints.toString(),
                                fiveDays_distance.toString(),
                                fiveDays_calories.toString()
                            )
                        }
                        periodical.toLowerCase() == "weekly" -> {
                            weeklyRecord()

                            writeNewPost(
                                user_id,
                                weekly_steps.toString(),
                                daily_duration.toString(),
                                daily_heartPoints.toString(),
                                weekly_distance.toString(),
                                weekly_calories.toString()
                            )
                        }
                    }

                }
            }, delay.toLong(), period.toLong())

            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()
            // ...
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // ...
        }
    }


    private fun threeDaysRecord(){


        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")
        val strDate = mdformat.format(calendar.time)
        calendar.add(Calendar.DATE, -1)
        val strDate1 = mdformat.format(calendar.time)
        calendar.add(Calendar.DATE, -1)
        val strDate2 = mdformat.format(calendar.time)

        Log.i(TAG, "strDate: $strDate")
        Log.i(TAG, "strDate1: $strDate1")
        Log.i(TAG, "strDate2: $strDate2")

        var date = sharedPreferences!!.getString("date", "2019-06-12")


        var checkDate: Int = 0

        if(date != strDate){

            var date1: Date = mdformat.parse(strDate)
            var date2: Date = mdformat.parse(date)

            checkDate = ((date1.time - date2.time)/ (1000 * 60 * 60 * 24)).toInt()

            if(checkDate > 3) {

                val editor = sharedPreferences?.edit()
                editor!!.putString("date", strDate)

                editor!!.commit()
            }


        }

        Log.i(TAG, "checkDate: $checkDate")


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$user_id")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>

                    var totalS = 0L
                    var totalD = 0L
                    var totalC = 0L

                    for((key, value) in my){
                        val details = value as Map<String, String>

                        if(strDate == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()

                        }
                        if(checkDate == 1) {
                            if(strDate1 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                        }
                        if(checkDate == 2) {

                            if(strDate1 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }

                            if(strDate2 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    Log.i(TAG, "total3days: $totalS")
                    threeDays_steps =  totalS
                    threeDays_distance = totalD
                    threeDays_calories = totalC
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

    }

    private fun fiveDaysRecord(){


        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")
        val strDate = mdformat.format(calendar.time)
        calendar.add(Calendar.DATE, -1)
        val strDate1 = mdformat.format(calendar.time)
        calendar.add(Calendar.DATE, -1)
        val strDate2 = mdformat.format(calendar.time)
        calendar.add(Calendar.DATE, -1)
        val strDate3 = mdformat.format(calendar.time)
        calendar.add(Calendar.DATE, -1)
        val strDate4 = mdformat.format(calendar.time)

        Log.i(TAG, "strDate: $strDate")
        Log.i(TAG, "strDate1: $strDate1")
        Log.i(TAG, "strDate2: $strDate2")
        Log.i(TAG, "strDate3: $strDate3")
        Log.i(TAG, "strDate4: $strDate4")


        var date = sharedPreferences!!.getString("date", "2019-06-12")


        var checkDate: Int = 0

        if(date != strDate){

            var date1: Date = mdformat.parse(strDate)
            var date2: Date = mdformat.parse(date)

            checkDate = ((date1.time - date2.time)/ (1000 * 60 * 60 * 24)).toInt()

            if(checkDate > 5) {

                val editor = sharedPreferences?.edit()
                editor!!.putString("date", strDate)

                editor!!.commit()
            }


        }

        Log.i(TAG, "checkDate: $checkDate")


        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$user_id")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>

                    var totalS = 0L
                    var totalD = 0L
                    var totalC = 0L

                    for((key, value) in my){
                        val details = value as Map<String, String>

                        if(strDate == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()

                        }
                        if(checkDate == 1) {
                            if(strDate1 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                        }
                        if(checkDate == 2) {
                            if(strDate1 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }

                            if(strDate2 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                        }
                        if(checkDate == 3) {
                            if(strDate1 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                            if(strDate2 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }

                            if(strDate3 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                        }
                        if(checkDate == 4) {

                            if(strDate1 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }

                            if(strDate2 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                            if(strDate3 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }

                            if(strDate4 == key) {
                                totalS += details["Steps"]!!.toLong()
                                totalD += details["Ms"]!!.toLong()
                                totalC += details["Cals"]!!.toLong()
                            }
                        }


                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    Log.i(TAG, "total3days: $totalS")
                    fiveDays_steps =  totalS
                    fiveDays_distance = totalD
                    fiveDays_calories = totalC
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

    }


    private fun weeklyRecord(){

        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val strDate = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        val strDate1 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
        val strDate2 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY)
        val strDate3 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val strDate4 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        val strDate5 = mdformat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val strDate6 = mdformat.format(calendar.time)

        Log.i(TAG, "strDate: $strDate")
        Log.i(TAG, "strDate1: $strDate1")
        Log.i(TAG, "strDate2: $strDate2")
        Log.i(TAG, "strDate3: $strDate3")
        Log.i(TAG, "strDate4: $strDate4")
        Log.i(TAG, "strDate5: $strDate5")
        Log.i(TAG, "strDate6: $strDate6")



        val myRef = FirebaseDatabase.getInstance().reference.child("DailyRecord/$user_id")

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                if(dataSnapshot.value != null) {
                    val my = dataSnapshot.value as Map<String, Any>

                    var totalS = 0L
                    var totalD = 0L
                    var totalC = 0L

                    for((key, value) in my){
                        val details = value as Map<String, String>

                        if(strDate == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()

                        }
                        if(strDate1 == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()
                        }
                        if(strDate2 == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()
                        }
                        if(strDate3 == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()

                        }
                        if(strDate4 == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()
                        }
                        if(strDate5 == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()
                        }
                        if(strDate6 == key) {
                            totalS += details["Steps"]!!.toLong()
                            totalD += details["Ms"]!!.toLong()
                            totalC += details["Cals"]!!.toLong()
                        }

                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    Log.i(TAG, "total3days: $totalS")
                    weekly_steps =  totalS
                    weekly_distance = totalD
                    weekly_calories = totalC
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRef.addValueEventListener(postListener)

    }


    private fun recordDaily(id: String, date: String, Cals: String, Goal: String, HPs: String, Minis: String, Ms: String, Rank: String, Steps: String, Token: String) {

        val childUpdates = java.util.HashMap<String, Any>()

//        if(Cals != "0") {
//            childUpdates["/DailyRecord/$id/$date/Cals"] = Cals
//        }
//
//        childUpdates["/DailyRecord/$id/$date/Goal"] = Goal
//
//        if(HPs != "0") {
//            childUpdates["/DailyRecord/$id/$date/HPs"] = HPs
//        }
//
//        if(Minis != "0") {
//            childUpdates["/DailyRecord/$id/$date/Minis"] = Minis
//        }
//
//        if(Ms != "0") {
//            childUpdates["/DailyRecord/$id/$date/Ms"] = Ms
//        }
//
//        childUpdates["/DailyRecord/$id/$date/Rank"] = Rank
//
//        if(Steps != "0") {
//            childUpdates["/DailyRecord/$id/$date/Steps"] = Steps
//        }
//
//        childUpdates["/DailyRecord/$id/$date/Token"] = Token



        childUpdates["/DailyRecord/$id/$date/Cals"] = Cals
        childUpdates["/DailyRecord/$id/$date/Goal"] = Goal
        childUpdates["/DailyRecord/$id/$date/HPs"] = HPs
        childUpdates["/DailyRecord/$id/$date/Minis"] = Minis
        childUpdates["/DailyRecord/$id/$date/Ms"] = Ms
        childUpdates["/DailyRecord/$id/$date/Rank"] = Rank
        childUpdates["/DailyRecord/$id/$date/Steps"] = Steps
        childUpdates["/DailyRecord/$id/$date/Token"] = Token

        Log.w(TAG, "recordDaily childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

    }

    private fun writeNewPost(id: String, currentSteps: String, duration:String, heartPoints:String, distance:String, calories:String) {
        val childUpdates = HashMap<String, Any>()

//        if(currentSteps != "0" ) {
//            childUpdates["/User/$id/currentSteps"] = currentSteps
//        }
//        if(duration != "0") {
//            childUpdates["/User/$id/duration"] = duration
//        }
//        if(heartPoints != "0") {
//            childUpdates["/User/$id/heartPoints"] = heartPoints
//        }
//        if(distance != "0") {
//            childUpdates["/User/$id/distance"] = distance
//        }
//        if(calories != "0") {
//            childUpdates["/User/$id/calories"] = calories
//
//            Log.w(TAG, "writeNewPostcalories: $calories")
//
//        }

        childUpdates["/User/$id/currentSteps"] = currentSteps

        childUpdates["/User/$id/duration"] = duration

        childUpdates["/User/$id/heartPoints"] = heartPoints

        childUpdates["/User/$id/distance"] = distance

        childUpdates["/User/$id/calories"] = calories

        Log.w(TAG, "writeNewPost childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
    }

    private fun writeTeamStepsPost(id: String, teamN: String, teamSteps: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/Teams/$teamN/teamSteps"] = teamSteps
        childUpdates["/User/$id/teamSteps"] = teamSteps

        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)
    }


    private fun getData(){
        //get Date and sync data
        mTimer = Timer()
        val delay = 0 // delay for 0 sec.
        val period = 5000 // repeat 5 sec.

        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.w(TAG, "sendData$user_id")

                sendData()

            }
        }, delay.toLong(), period.toLong())
    }


    private fun sendData() {

        this?.runOnUiThread {

            val allSteps = HashMap<String, Any>()

            allSteps["user_id"] = user_id
            allSteps["periodical"] = periodical
            allSteps["team"] = team
            allSteps["captain"] = captain
            allSteps["teamRank"] = teamRank

            allSteps["teammate_steps"] = teammate_steps
            allSteps["my_steps"] = my_steps
            allSteps["my_heartPoints"] = my_heartPoints
            allSteps["my_duration"] = my_duration
            allSteps["my_distance"] = my_distance
            allSteps["my_calories"] = my_calories

            allSteps["team_steps"] = team_steps

            allSteps["my_goal"] = my_goal
            allSteps["my_rank"] = my_rank
            allSteps["teammate_goal"] = teammate_goal
            allSteps["team_goal"] = team_goal


            model!!.setMsgCommunicator(allSteps.toString())
            Log.i(TAG, "MainActivity allSteps test : $allSteps")
        }
    }

    fun onPasswordChanged() {
        showSnackBarMessage("Password Changed Successfully !")
    }

    fun onPasswordReset(message: String) {
        showSnackBarMessage(message)
    }

    private fun showSnackBarMessage(message: String) {
        Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onConnected(bundle: Bundle?) {
        val dataSourcesRequest = DataSourcesRequest.Builder()
            .setDataTypes(DataType.TYPE_MOVE_MINUTES)
            .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
            .setDataTypes(DataType.TYPE_HEART_POINTS)
            .setDataTypes(DataType.TYPE_DISTANCE_DELTA)
            .setDataTypes(DataType.TYPE_CALORIES_EXPENDED)
            .setDataSourceTypes(DataSource.TYPE_RAW)
            .build()

        val dataSourcesResultCallback =
            ResultCallback<DataSourcesResult> { dataSourcesResult ->
//                for (dataSource in dataSourcesResult.dataSources) {
//                    val type = dataSource.dataType
//
//                    if (DataType.TYPE_STEP_COUNT_DELTA == type || DataType.TYPE_STEP_COUNT_CUMULATIVE == type) {
//                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_DELTA)
//                    }
//                }
            }

        Fitness.SensorsApi.findDataSources(mClient, dataSourcesRequest)
            .setResultCallback(dataSourcesResultCallback)

    }

    override fun onConnectionSuspended(i: Int) {
        // TODO move to subclass so that we can disable UI components, etc., in the event that the service is inaccessible
        // If your connection to the client gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "GoogleApiClient connection lost. Reason: Network lost.")
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "GoogleApiClient connection lost. Reason: Service disconnected")
        }
    }



    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true
                connectionResult.startResolutionForResult(this, REQUEST_OAUTH)
            } catch (e: IntentSender.SendIntentException) {
                Log.e("GoogleFit", "sendingIntentException " + e.message)
            }

        } else {
            Log.e("GoogleFit", "authInProgress")
        }
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    // register BroadcastReceiver
    private fun registerMyReceiver() {

        val filter = IntentFilter()
        filter.addAction("com.vt.BroadcastReceiver")
        registerReceiver(receiver, filter)

    }

    // register MyNotificationService
    private fun startMyNotificationService() {

        val notificationService = MyNotificationService::class.java

        val intent = Intent(applicationContext, notificationService)

        intent.putExtra("user_id", user_id)
        intent.putExtra("team", team)

        if(loginStatus == 1){
            if (!isServiceRunning(notificationService)) {
                // Start the service
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Log.i(TAG, "Start MyNotificationService.")
            } else {
                Log.i(TAG, "MyNotificationService already running.")
            }
        }

    }

    private fun stopMyNotificationService(){

        val notificationService = MyNotificationService::class.java
        val intent = Intent(applicationContext, notificationService)

        intent.putExtra("user_id", "none")
        intent.putExtra("team", "none")

        applicationContext.stopService(intent)
        Log.i(TAG, "Stop MyNotificationService.")

    }


    // Custom method to determine whether a service is running
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Loop through the running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                // If the service is running then return true
                return true
            }
        }
        return false
    }

    override fun onStart() {
        super.onStart()
        mClient!!.connect()

        Log.i(TAG, "myNotificationServiceStatus: $myNotificationServiceStatus")
        if(myNotificationServiceStatus == "startMyNotificationService") {
            startMyNotificationService()
        }
        else {
            stopMyNotificationService()
        }

        registerMyReceiver()

        val editor = sharedPreferences?.edit()
        editor!!.putString("tabLayoutPeriodical", "all")

        editor.commit()
    }

    override fun onStop() {
        super.onStop()
        mClient!!.disconnect()
        unregisterReceiver(receiver)

        val editor = sharedPreferences!!.edit()

        editor.remove("my_rank")
        editor.remove("currentSteps")
        editor.remove("duration")
        editor.remove("heartPoints")
        editor.remove("distance")
        editor.remove("calories")

        editor.commit()
    }

    override fun onPause() {
        super.onPause()
        mClient!!.stopAutoManage(this)
        mClient!!.disconnect()
    }

    override fun onResume() {
        super.onResume()
        mClient!!.connect()
    }

    override fun onBackPressed() {
        if(Navigation.findNavController(this, R.id.my_nav_host_fragment).currentDestination!!.id == R.id.loginFragment ||
            Navigation.findNavController(this, R.id.my_nav_host_fragment).currentDestination!!.id == R.id.homeFragment ||
            Navigation.findNavController(this, R.id.my_nav_host_fragment).currentDestination!!.id == R.id.historyFragment ||
            Navigation.findNavController(this, R.id.my_nav_host_fragment).currentDestination!!.id == R.id.teamFragment ||
            Navigation.findNavController(this, R.id.my_nav_host_fragment).currentDestination!!.id == R.id.awardsFragment ||
            Navigation.findNavController(this, R.id.my_nav_host_fragment).currentDestination!!.id == R.id.settingFragment) {

            moveTaskToBack(true)

        }
        else {

            Navigation.findNavController(this, R.id.my_nav_host_fragment).navigateUp()

        }

    }
}