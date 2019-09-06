package com.vt.fitaware

import android.app.AlertDialog
import android.app.Service
import android.content.*
import android.os.*
import android.preference.PreferenceManager
import android.util.Log
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.vt.fitaware.Home.Teammates
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyBackgroundService : Service() {

    private val TAG = "MyBackgroundService"

    private var mClient: GoogleApiClient? = null
    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"

    private var mTimer: Timer? = null
    private var daily_steps: Long = 0
    private var daily_heartPoints: Long = 0
    private var daily_duration: Long = 0
    private var daily_calories: Long = 0
    private var daily_distance: Long = 0

    private var periodical: String = "none"

    private var threeDays_steps: Long = 0
    private var threeDays_distance: Long = 0
    private var threeDays_calories: Long = 0

    private var fiveDays_steps: Long = 0
    private var fiveDays_distance: Long = 0
    private var fiveDays_calories: Long = 0

    private var weekly_steps: Long = 0
    private var weekly_distance: Long = 0
    private var weekly_calories: Long = 0

    private var my_rank: String = " "
    private var my_goal: Long = 0
    private var token = "none"

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented") as Throwable
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        Log.i(TAG, "MyBackgroundService started.")

        Log.d(TAG,intent.extras.getString("user_id"))
        Log.d(TAG,intent.extras.getString("periodical"))
        Log.d(TAG,intent.extras.getLong("my_goal", 0).toString())
        Log.d(TAG,intent.extras.getString("my_rank").toString())

        user_id = intent.extras.getString("user_id")
        periodical = intent.extras.getString("periodical")
        my_goal = intent.extras.getLong("my_goal", 0)
        my_rank = intent.extras.getString("my_rank")


        mClient!!.connect()
        Log.i(TAG, "mClient connected.")
        registerMyBackgroundServiceReceiver()

        val fitnessDataTask = FitnessDataTask()
        fitnessDataTask.execute()

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onCreate() {
        super.onCreate()
        initSharedPreferences()


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

//                Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
            })

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
            .build()

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

                    Log.i(TAG, "Total duration: $resultDuration")
                    Log.i(TAG, "Total duration: $totalResultDuration")

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

                    Log.i(TAG, "Total steps: $resultSteps")
                    Log.i(TAG, "Total steps: $totalResultSteps")

                    Log.i(TAG, "Total steps: $totalSteps")

                    daily_steps = totalSteps


                    var totalHeartPoints: Long = 0

                    val resultHeartPoints = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_HEART_POINTS)
                    val totalResultHeartPoints = resultHeartPoints.await(30, TimeUnit.SECONDS)
//                    if (totalResultHeartPoints.status.isSuccess) {
//                        val totalSetHeartPoints = totalResultHeartPoints.total
//                        totalHeartPoints = (if (totalSetHeartPoints!!.isEmpty)
//                            0
//                        else
//                            totalSetHeartPoints.dataPoints[0].getValue(Field.FIELD_MAX).asInt()).toLong()
//                    } else {
//                        Log.w(TAG, "There was a problem getting the HeartPoints.")
//                    }

                    Log.i(TAG, "Total HeartPoints: $resultHeartPoints")
                    Log.i(TAG, "Total HeartPoints: $totalResultHeartPoints")

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

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private inner class MyBackgroundServiceReceiver : BroadcastReceiver() {
        private val TAG = "MyBackgroundServiceReceiver"

        override fun onReceive(context: Context, intent: Intent) {

            my_rank = intent.getStringExtra("my_rank")
            Log.i(TAG, "BroadcastReceiver my_rank: $my_rank")

        }
    }

    // register MyBackgroundServiceReceiver
    private fun registerMyBackgroundServiceReceiver() {

        val filter = IntentFilter()
        filter.addAction("com.vt.MyBackgroundServiceReceiver")
        registerReceiver(MyBackgroundServiceReceiver(), filter)

    }

    override fun onDestroy() {

        mClient!!.disconnect()
        Log.i(TAG, "mClient disconnected.")

        super.onDestroy()
        Log.i(TAG, "MyBackgroundService Stopped.")


    }
}
