package com.vt.fitaware

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
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

class MyBackgroundWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val TAG = "MyBackgroundWorker"

    private var mClient: GoogleApiClient? = null
    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"

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

    private var loginStatus = 1
    private var team: String = "none"

    private var myNotificationServiceStatus: String = "startMyNotificationService"

    private var allUsers = ArrayList<Teammates>(1)

    override fun doWork(): Result {

        initSharedPreferences()

        // initial all the values
        user_id = sharedPreferences!!.getString("user_id", "none")
        my_goal = sharedPreferences!!.getString("my_goal", "0").toLong()
        periodical = sharedPreferences!!.getString("periodical", "none")

        loginStatus = sharedPreferences!!.getInt("loginStatus", 0)
        team = sharedPreferences!!.getString("team", "none")

        myNotificationServiceStatus = sharedPreferences!!.getString("MyNotificationServiceStatus", "startMyNotificationService")

        mClient = GoogleApiClient.Builder(applicationContext)
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

        mClient!!.connect()
        Log.i(TAG, "mClient connected.")

        return try {
            Log.d(TAG, "doWork: work is done")
            Log.d(TAG, "user_id: $user_id")

            getDeviceToken()
            getRank()
            getGoogleFitAPITasks()

            Log.i(TAG, "myNotificationServiceStatus: $myNotificationServiceStatus")
            if(myNotificationServiceStatus == "startMyNotificationService") {
                startMyNotificationService()
            }
            else {
                stopMyNotificationService()
            }

            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
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
                    applicationContext.startForegroundService(intent)
                } else {
                    applicationContext.startService(intent)
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
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Loop through the running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                // If the service is running then return true
                return true
            }
        }
        return false
    }

    fun getDeviceToken() {
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
                val msg = "FCM Token: $token"
                Log.d(TAG, msg)

                val editor = sharedPreferences?.edit()
                editor!!.putString("token", token)

                editor!!.commit()
            })
    }

    fun getRank() {
        val myRef = FirebaseDatabase.getInstance().reference.child("User")
        val myPostListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>
                Log.i(TAG, "user: $my")

                allUsers.clear()
                for ((key, value) in my) {
                    val details = value as Map<String, String>
                    allUsers.add(Teammates("1",  key, "0", details.getValue("currentSteps").toInt(), details["goal"], details.getValue("duration").toInt(), details.getValue("heartPoints").toInt(), details.getValue("distance").toInt(), details.getValue("calories").toInt(), "#3ebfab"))
                }

                // get my_rank from all Users
                val allUsersSort = allUsers.sortedWith(compareByDescending(Teammates::getSteps))
                allUsers = ArrayList(allUsersSort)
                var indexM = 1
                for(users in allUsers) {
                    users.rank = indexM.toString()

                    if(users.name == user_id) {
                        my_rank = users.rank
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
    }


    fun getGoogleFitAPITasks() {

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


        childUpdates["/User/$id/currentSteps"] = currentSteps

        childUpdates["/User/$id/duration"] = duration

        childUpdates["/User/$id/heartPoints"] = heartPoints

        childUpdates["/User/$id/distance"] = distance

        childUpdates["/User/$id/calories"] = calories

        Log.w(TAG, "writeNewPost childUpdates: $childUpdates")

        FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }
}
