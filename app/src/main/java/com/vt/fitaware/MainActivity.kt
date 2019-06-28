package com.example.fitaware

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.*
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import androidx.navigation.Navigation
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import com.example.fitaware.FirebaseMessagingService.MyReceiver
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
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {



    private val TAG = "MainActivity"
    private lateinit var database: DatabaseReference
    private var mClient: GoogleApiClient? = null
    private var receiver: BroadcastReceiver = MyReceiver()
    private var authInProgress = false
    private var REQUEST_OAUTH = 1

    var loginStatus = 1

    private var captain: String = "none"

    private var daily_steps: Long = 0
    private var daily_heartPoints: Long = 0
    private var daily_duration: Long = 0
    private var daily_calories: Long = 0
    private var daily_distance: Long = 0

    private var user_id: String = "none"

    private var periodical: String = "none"
    private var team: String = "none"

    private var my_rank: String = "0"
    private var my_duration: Long = 0
    private var my_heartPoints: Long = 0
    private var my_calories: Long = 0
    private var my_distance: Long = 0

    private var sharedPreferences: SharedPreferences? = null

    private var my_steps: Long = 0
    private var teammate_steps: Long = 0
    private var team_steps: Long = 0

    private var my_goal: Long = 0
    private var teammate_goal: Long = 0
    private var team_goal: Long = 0

    private var model: Communicator?=null
    private var mTimer: Timer? = null
    private var bottomNavigationView: BottomNavigationView? = null



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



    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSharedPreferences()

        // initial all the values
        user_id = sharedPreferences!!.getString("user_id", "none")
        team = sharedPreferences!!.getString("team", "none")
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        team_goal = sharedPreferences!!.getString("team_goal", "0").toLong()
        captain = sharedPreferences!!.getString("captain", "none")

        my_rank = sharedPreferences!!.getString("rank", "0")
        my_steps = sharedPreferences!!.getString("currentSteps", "0").toLong()
        my_duration = sharedPreferences!!.getString("duration", "0").toLong()
        my_heartPoints = sharedPreferences!!.getString("heartPoints", "0").toLong()
        my_distance = sharedPreferences!!.getString("distance", "0").toLong()
        my_calories = sharedPreferences!!.getString("calories", "0").toLong()

        loginStatus = sharedPreferences!!.getInt("loginStatus", 0)
        Log.i(TAG, "teamName: $team")

        Log.i(TAG, "sharedPreferences my_goal: $my_goal")
        Log.i(TAG, "user_id: $user_id")
        Log.i(TAG, "loginStatus: $loginStatus")

        Log.i(TAG, "sharedPreferences my_steps: $my_steps")
        Log.i(TAG, "sharedPreferences my_duration: $my_duration")
        Log.i(TAG, "sharedPreferences my_heartPoints: $my_heartPoints")
        Log.i(TAG, "sharedPreferences my_distance: $my_distance")
        Log.i(TAG, "sharedPreferences my_calories: $my_calories")

        //send Date and sync data
        mTimer = Timer()
        val delay = 3000 // delay for 0 sec.
        val period = 5000 // repeat 5 sec.

        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.w(TAG, "sendData$user_id")

                val durationDataTask = DurationDataTask()
                val stepsDataTask = StepsDataTask()
                val heartPointsDataTask = HeartPointsDataTask()
                val caloriesDataTask = CaloriesDataTask()
                val distanceDataTask = DistanceDataTask()

                durationDataTask.execute()
                stepsDataTask.execute()
                heartPointsDataTask.execute()
                caloriesDataTask.execute()
                distanceDataTask.execute()

                sendData()
            }
        }, delay.toLong(), period.toLong())

        // Firebase notification token
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)
//                Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
            })



        bottomNavigationView = findViewById<View>(R.id.bottomNavigation) as BottomNavigationView
        bottomNavigationView!!.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar!!.title = ""


        // get user_id
//        receiveData()


        if(loginStatus == 1 && user_id != "none") {

            val myRef = FirebaseDatabase.getInstance().reference.child("User")
            val myPostListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>
                    Log.i(TAG, "user: $my")

                    var iniTeamSteps = 0L

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
                            team_goal = details["teamGoal"].toString().toLong()

                            iniTeamSteps += my_steps

                        }
                        if(details["team"].toString() != "none") {
                            if(details.getValue("team") == team && key != user_id){
                                iniTeamSteps += details["currentSteps"].toString().toLong()
                            }

                        }

                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    team_steps = iniTeamSteps
                    Log.i(TAG, "writeTeamStepsPost captain: $captain")

                    if (captain != "none") {
                        writeTeamStepsPost(user_id, team, team_steps.toString())
                    }


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            myRef.addValueEventListener(myPostListener)
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


        Log.w(TAG, "user_id"+user_id)





        val allSteps = HashMap<String, Any>()

        allSteps["user_id"] = user_id
        allSteps["periodical"] = periodical
        allSteps["team"] = team
        allSteps["captain"] = captain

        allSteps["teammate_steps"] = teammate_steps
        allSteps["my_steps"] = my_steps
        allSteps["my_heartPoints"] = my_heartPoints
        allSteps["my_duration"] = my_duration
        allSteps["my_distance"] = my_distance
        allSteps["my_calories"] = my_calories

        allSteps["team_steps"] = team_steps

        allSteps["my_goal"] = my_goal
        allSteps["teammate_goal"] = teammate_goal
        allSteps["team_goal"] = team_goal



        model= ViewModelProviders.of(this).get(Communicator::class.java)
        model!!.setMsgCommunicator(allSteps.toString())
        Log.w(TAG, "allSteps$allSteps")

    }


    private fun writeNewPost(id: String, currentSteps: String, duration:String, heartPoints:String, distance:String, calories:String) {
        val childUpdates = HashMap<String, Any>()
        val editor = sharedPreferences?.edit()

        if(currentSteps != "0" ) {
            childUpdates["/User/$id/currentSteps"] = currentSteps
            editor!!.putString("currentSteps", currentSteps)
            editor.commit()

        }
        if(duration != "0") {
            childUpdates["/User/$id/duration"] = duration
            editor!!.putString("duration", duration)
            editor.commit()

        }
        if(heartPoints != "0") {
            childUpdates["/User/$id/heartPoints"] = heartPoints
            editor!!.putString("heartPoints", heartPoints)
            editor.commit()

        }
        if(distance != "0") {
            childUpdates["/User/$id/distance"] = distance
            editor!!.putString("distance", distance)
            editor.commit()

        }
        if(calories != "0") {
            childUpdates["/User/$id/calories"] = calories
            editor!!.putString("calories", calories)
            editor.commit()

            Log.w(TAG, "writeNewPostcalories: $calories")

        }

        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)
    }

    private fun writeTeamStepsPost(id: String, teamN: String, teamSteps: String) {
        val childUpdates = HashMap<String, Any>()

        childUpdates["/Teams/$teamN/teamSteps"] = teamSteps
        childUpdates["/User/$id/teamSteps"] = teamSteps

        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)
    }



    private fun sendData() {

        this?.runOnUiThread {

            if(user_id != "none") {
                writeNewPost(user_id, daily_steps.toString(), daily_duration.toString(), daily_heartPoints.toString(), daily_distance.toString(), daily_calories.toString())

            }


            val allSteps = HashMap<String, Any>()

            allSteps["user_id"] = user_id
            allSteps["periodical"] = periodical
            allSteps["team"] = team
            allSteps["captain"] = captain

            allSteps["teammate_steps"] = teammate_steps
            allSteps["my_steps"] = my_steps
            allSteps["my_heartPoints"] = my_heartPoints
            allSteps["my_duration"] = my_duration
            allSteps["my_distance"] = my_distance
            allSteps["my_calories"] = my_calories
            allSteps["team_steps"] = team_steps

            allSteps["my_goal"] = my_goal
            allSteps["teammate_goal"] = teammate_goal
            allSteps["team_goal"] = team_goal



            model!!.setMsgCommunicator(allSteps.toString())
            Log.i(TAG, "MainActivity allSteps : $allSteps")
        }
    }

    private inner class DurationDataTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            var total: Long = 0

            val result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_MOVE_MINUTES)
            val totalResult = result.await(30, TimeUnit.SECONDS)
            if (totalResult.status.isSuccess) {
                val totalSet = totalResult.total
                total = (if (totalSet!!.isEmpty)
                    0
                else
                    totalSet.dataPoints[0].getValue(Field.FIELD_DURATION).asInt()).toLong()
            } else {
                Log.w(TAG, "There was a problem getting the duration.")
            }

            Log.i(TAG, "Total duration: $total")

            daily_duration = total

            return null
        }
    }

    private inner class StepsDataTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            var total: Long = 0

            val result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA)
            val totalResult = result.await(30, TimeUnit.SECONDS)
            if (totalResult.status.isSuccess) {
                val totalSet = totalResult.total
                total = (if (totalSet!!.isEmpty)
                    0
                else
                    totalSet.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()).toLong()
            } else {
                Log.w(TAG, "There was a problem getting the step count.")
            }

            Log.i(TAG, "Total steps: $total")

            daily_steps = total

            return null
        }
    }

    private inner class HeartPointsDataTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            var total: Long = 0

            val result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_HEART_POINTS)
            val totalResult = result.await(30, TimeUnit.SECONDS)
            if (totalResult.status.isSuccess) {
                val totalSet = totalResult.total
                total = (if (totalSet!!.isEmpty)
                    0
                else
                    "%.0f".format(totalSet.dataPoints[0].getValue(Field.FIELD_DURATION).asFloat())).toString().toLong()
            } else {
                Log.w(TAG, "There was a problem getting the HeartPoints.$result $totalResult")
            }

            Log.i(TAG, "Total HeartPoints: $total")

            daily_heartPoints = total

            return null
        }
    }


    private inner class DistanceDataTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            var total: Long = 0

            val result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_DISTANCE_DELTA)
            val totalResult = result.await(30, TimeUnit.SECONDS)
            if (totalResult.status.isSuccess) {
                val totalSet = totalResult.total
                total = (if (totalSet!!.isEmpty)
                    0
                else
                    "%.0f".format(totalSet.dataPoints[0].getValue(Field.FIELD_DISTANCE).asFloat())).toString().toLong()
            } else {
                Log.w(TAG, "There was a problem getting the Distance.")
            }

            Log.i(TAG, "Total Distance: $total")

            daily_distance = total

            return null
        }
    }


    private inner class CaloriesDataTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {

            var total: Long = 0

            val result = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_CALORIES_EXPENDED)
            val totalResult = result.await(30, TimeUnit.SECONDS)
            if (totalResult.status.isSuccess) {
                val totalSet = totalResult.total
                total = (if (totalSet!!.isEmpty)
                    0
                else
                    "%.0f".format(totalSet.dataPoints[0].getValue(Field.FIELD_CALORIES).asFloat())).toString().toLong()
            } else {
                Log.w(TAG, "There was a problem getting the Calories.")
            }

            Log.i(TAG, "Total Calories: $total")

            daily_calories = total

            return null
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
        val dataSourcesResultCallback = ResultCallback<DataSourcesResult> {
            //                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
            //
            //                    if( DataType.TYPE_STEP_COUNT_CADENCE.equals( dataSource.getDataType() ) ) {
            //                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CADENCE);
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
        filter.addAction("BroadcastReceiver")
        registerReceiver(receiver, filter)

    }

    override fun onStart() {
        super.onStart()
        mClient!!.connect()
        registerMyReceiver()

        val editor = sharedPreferences?.edit()
        editor!!.putString("tabLayoutPeriodical", "all")

        editor.commit()
    }

    override fun onStop() {
        super.onStop()
        mClient!!.disconnect()
        unregisterReceiver(receiver)
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
