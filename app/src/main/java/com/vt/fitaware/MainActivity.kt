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

    private val workManager: WorkManager = WorkManager.getInstance()


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

        val myBackgroundWorker = PeriodicWorkRequest.Builder(
            MyBackgroundWorker::class.java,
            15,
            TimeUnit.MINUTES
        ).build()

        workManager.enqueue(myBackgroundWorker)

        // initial all the values
        user_id = sharedPreferences!!.getString("user_id", "none")
        team = sharedPreferences!!.getString("team", "none")
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        my_rank = sharedPreferences!!.getString("my_rank", " ")
        team_goal = sharedPreferences!!.getString("team_goal", "0").toLong()
        captain = sharedPreferences!!.getString("captain", "none")
        periodical = sharedPreferences!!.getString("periodical", "none")

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


        if(loginStatus == 1 && user_id != "none") {

            //get date from google api
            getData()


            val myRef = FirebaseDatabase.getInstance().reference.child("User")
            val myPostListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>
                    Log.i(TAG, "user: $my")

                    var iniTeamSteps = 0L
                    var index = 1

                    allUsers.clear()

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

                        allUsers.add(Teammates("1",  key, index.toString(), details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#3ebfab"))


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


                    // get my_rank from all Users
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
            myRef.addValueEventListener(myPostListener)


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
                                childUpdates["/User/$user_id/teamGoal"] = team_goal
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


        Log.w(TAG, "user_id: "+user_id)

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

    //register MyBackgroundService
    private fun startMyBackgroundService() {

        val myBackgroundService = MyBackgroundService::class.java

        val intent = Intent(applicationContext, myBackgroundService)

        intent.putExtra("user_id", user_id)
        intent.putExtra("periodical", periodical)
        intent.putExtra("my_goal", my_goal)
        intent.putExtra("my_rank", my_rank)


        if(loginStatus == 1){
            if (!isServiceRunning(myBackgroundService)) {
                // Start the service
                startService(intent)
                Log.i(TAG, "Start MyBackgroundService.")
            } else {
                Log.i(TAG, "MyBackgroundService already running.")
            }
        }

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
        startMyNotificationService()
        startMyBackgroundService()
        registerMyReceiver()

//        val editor = sharedPreferences?.edit()
//        editor!!.putString("tabLayoutPeriodical", "all")
//
//        editor.commit()
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