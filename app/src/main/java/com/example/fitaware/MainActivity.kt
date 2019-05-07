package com.example.fitaware

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.TextView
import androidx.navigation.Navigation
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import com.example.fitaware.Home.MemberBriefAdapter
import com.example.fitaware.Home.TeamMemberListFragment
import com.example.fitaware.Team.Member
import com.example.fitaware.model.User
import com.example.fitaware.utils.Constants
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
import com.google.firebase.database.*
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.fragment_setting.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, TeamMemberListFragment.OnCompleteListener  {



    private val TAG = "MainActivity"
    private lateinit var database: DatabaseReference
    private var mClient: GoogleApiClient? = null
    private var authInProgress = false
    private var REQUEST_OAUTH = 1

    var loginStatus = 0

    var captain: String? = null

    private var daily_steps: Long = 0

    private var user_id: String = ""
    private var selected_id: String = "Display None"

    private var periodical: String = "none"


    private var my_steps: Long = 0
    private var teammate_steps: Long = 0
    private var team_steps: Long = 0

    private var my_goal: Long = 0
    private var teammate_goal: Long = 0
    private var team_goal: Long = 0

    private var model: Communicator?=null
    private var mTimer: Timer? = null



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
            R.id.navigation_me -> {
//                val actionBar = supportActionBar
//                actionBar?.show()

                val toolbarTiltle = findViewById<TextView>(R.id.toolbar_title)
                toolbarTiltle.text = "Me"

                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.meFragment)

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<View>(R.id.bottomNavigation) as BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        setSupportActionBar(findViewById(R.id.toolbar))
        val actionBar = supportActionBar
        actionBar!!.title = ""


        receiveData()

        if (loginStatus == 1) {
            bottomNavigationView.visibility = View.VISIBLE
            Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.homeFragment)

        } else {
            bottomNavigationView.visibility = View.GONE
            Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.loginFragment)
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
        Log.w(TAG, "loginStatus"+loginStatus)


        if(loginStatus == 1 && intent.getStringExtra("user_id") != null) {
            val myRef = FirebaseDatabase.getInstance().reference.child("User")
            val myPostListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val my = dataSnapshot.value as Map<String, Any>

                    var iniTeamSteps = 0L
                    var iniTeamGoal = 0L

                    for((key, value) in my){
                        val details = value as Map<String, String>

                        if(key == user_id) {
                            captain = details["captain"].toString()
                            if(!captain.equals("none")) {
                                bottomNavigationView.menu.getItem(1).setIcon(R.drawable.ic_captain_24dp)
                            }

                            my_steps = details["currentSteps"].toString().toLong()
                            my_goal = details["goal"].toString().toLong()
                            periodical = details["periodical"].toString()
                        }


                        iniTeamSteps += details["currentSteps"].toString().toLong()
                        iniTeamGoal += details["goal"].toString().toLong()




                        Log.i(TAG, "$key: $value")
                        Log.i(TAG, "details: $details")

                    }

                    team_steps = iniTeamSteps
                    team_goal = iniTeamGoal

                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            myRef.addValueEventListener(myPostListener)

            Log.w(TAG, "selected_idOnCreate"+selected_id)
        }





        if(intent.getStringExtra("user_id") != null) {
            val allSteps = HashMap<String, Any>()

            allSteps["user_id"] = user_id
            allSteps["periodical"] = periodical

            allSteps["teammate_steps"] = teammate_steps
            allSteps["my_steps"] = my_steps
            allSteps["team_steps"] = team_steps

            allSteps["my_goal"] = my_goal
            allSteps["teammate_goal"] = teammate_goal
            allSteps["team_goal"] = team_goal

            allSteps["selected_id"] = selected_id


            model= ViewModelProviders.of(this).get(Communicator::class.java)
            model!!.setMsgCommunicator(allSteps.toString())
            Log.w(TAG, "allSteps$allSteps")
        }

        //sendData
        mTimer = Timer()
        val delay = 1000 // delay for 0 sec.
        val period = 5000 // repeat 5 sec.

        mTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.w(TAG, "sendData"+user_id)

                val stepsDataTask = StepsDataTask()
                stepsDataTask.execute()

                sendData()
            }
        }, delay.toLong(), period.toLong())

    }


    override fun onComplete(selectedName: String) {
        selected_id = selectedName

        if(selected_id != "Display None") {
            val teammateRef = FirebaseDatabase.getInstance().reference.child("User/$selected_id")
            val teammatePostListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val teammate = dataSnapshot.value as Map<String, Any>

                    teammate_steps = teammate["currentSteps"].toString().toLong()
                    teammate_goal = teammate["goal"].toString().toLong()


                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                    // ...
                }
            }
            teammateRef.addValueEventListener(teammatePostListener)
            Log.w(TAG, "selected_id: $selected_id")
        }
    }

    private fun receiveData() {
        //RECEIVE DATA VIA INTENT
        val intent = intent
        if(intent.getStringExtra("user_id") != null) {
            Log.w(TAG, "intent: $intent")

            user_id = intent.getStringExtra("user_id")
        }
        loginStatus = intent.getIntExtra("Login_Status", 0)

    }

    private fun writeNewPost(id: String, currentSteps: String) {
        val childUpdates = HashMap<String, Any>()
        if(currentSteps.toInt() != 0){
            childUpdates["/User/$id/currentSteps"] = currentSteps
        }
        else {

        }
        Log.w(TAG, "childUpdates: $childUpdates")

        database.updateChildren(childUpdates)
    }

    private fun sendData() {

        this?.runOnUiThread {


            if(intent.getStringExtra("user_id") != null) {
                writeNewPost(user_id, daily_steps.toString())

                val allSteps = HashMap<String, Any>()

                allSteps["user_id"] = user_id
                allSteps["periodical"] = periodical

                allSteps["teammate_steps"] = teammate_steps
                allSteps["my_steps"] = my_steps
                allSteps["team_steps"] = team_steps

                allSteps["my_goal"] = my_goal
                allSteps["teammate_goal"] = teammate_goal
                allSteps["team_goal"] = team_goal

                allSteps["selected_id"] = selected_id


                model!!.setMsgCommunicator(allSteps.toString())
                Log.i(TAG, "MainActivity allSteps : $allSteps")
            }
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
            .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
            .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
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

    override fun onStart() {
        super.onStart()
        mClient!!.connect()
    }

    override fun onStop() {
        super.onStop()
        mClient!!.disconnect()
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

}
