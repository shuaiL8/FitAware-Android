package com.vt.fitaware

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews
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

    private var fireBase_steps = "none"
    private var fireBase_heartPoints = "none"
    private var fireBase_duration = "none"
    private var fireBase_calories = "none"
    private var fireBase_distance = "none"


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

    private var my_rank: String = "1"
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

//        my_rank = sharedPreferences!!.getString("my_rank", "1")

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
//            getRank()
            getGoogleFitAPITasks()

            Log.i(TAG, "myNotificationServiceStatus: $myNotificationServiceStatus")
            if(myNotificationServiceStatus == "startMyNotificationService") {
                startMyNotificationService()
            }
            else {
                stopMyNotificationService()
            }

            val calendar = Calendar.getInstance()
            val mdformat = SimpleDateFormat("HH:mm:ss")
            val curTime = mdformat.format(calendar.time).toString()
            Log.d(TAG, "curTime: $curTime")

            val from =  mdformat.parse("17:00:00").toString()
            val to = mdformat.parse("19:00:00").toString()

            // Test use
//            sendNotification()

            if(curTime > from && curTime < to) {
                Log.d(TAG, "curTime2: $curTime")
                getReportData()
                sendNotification()
            }

            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }

    private fun sendNotification() {

        val collapsedView = RemoteViews(applicationContext.packageName, R.layout.report_notification_collapsed)
        collapsedView.setTextViewText(R.id.textMySteps, fireBase_steps)
        collapsedView.setTextViewText(R.id.textMyMins, fireBase_duration)
        collapsedView.setTextViewText(R.id.textMyHPs, fireBase_heartPoints)
        collapsedView.setTextViewText(R.id.textMyDis, fireBase_distance)
        collapsedView.setTextViewText(R.id.textMyCals, fireBase_calories)

        val expandedView = RemoteViews(applicationContext.packageName, R.layout.report_notification_expended)
        expandedView.setTextViewText(R.id.textMySteps, "$fireBase_steps Steps")
        expandedView.setTextViewText(R.id.textMyMins, "$fireBase_duration Mins")
        expandedView.setTextViewText(R.id.textMyHPs, "$fireBase_heartPoints HPs")
        expandedView.setTextViewText(R.id.textMyDis, "$fireBase_distance Ms")
        expandedView.setTextViewText(R.id.textMyCals, "$fireBase_calories Cals")

        val CHANNEL_ID = "Report Notification"
        if(Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Report Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle("Progress Report")
            .setContentText("")
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(applicationContext, 0, Intent(applicationContext, MainActivity::class.java), 0))
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(expandedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
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

    fun getReportData() {
        val myRefUser = FirebaseDatabase.getInstance().reference.child("User")
        val myPostListenerUser = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>
                Log.i(TAG, "user: $my")

                for ((key, value) in my) {
                    val details = value as Map<String, String>
                    if(key == user_id) {
                        fireBase_steps = details.getValue("currentSteps")
                        fireBase_duration = details.getValue("duration")
                        fireBase_heartPoints = details.getValue("heartPoints")
                        fireBase_calories = details.getValue("calories")
                        fireBase_distance =details.getValue("distance")
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        }
        myRefUser.addValueEventListener(myPostListenerUser)
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
        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("yyyy-MM-dd")
//                    mdformat.timeZone = TimeZone.getTimeZone("America/New_York")
        val strDate = mdformat.format(calendar.time)

        val myRefDailyRecord = FirebaseDatabase.getInstance().reference.child("DailyRecord")
        val postListenerDailyRecord = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val my = dataSnapshot.value as Map<String, Any>

                allUsers.clear()

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


//        when {
//            periodical.toLowerCase() == "daily" -> {
//
//                writeNewPost(
//                    user_id,
//                    daily_steps.toString(),
//                    daily_duration.toString(),
//                    daily_heartPoints.toString(),
//                    daily_distance.toString(),
//                    daily_calories.toString()
//                )
//
//            }
//
//            periodical == "3 days" -> {
//
//                threeDaysRecord()
//
//                writeNewPost(
//                    user_id,
//                    threeDays_steps.toString(),
//                    daily_duration.toString(),
//                    daily_heartPoints.toString(),
//                    threeDays_distance.toString(),
//                    threeDays_calories.toString()
//                )
//
//
//                Log.i(TAG, "threeDays_steps: $threeDays_steps")
//
//            }
//            periodical == "5 days" -> {
//
//                fiveDaysRecord()
//
//                writeNewPost(
//                    user_id,
//                    fiveDays_steps.toString(),
//                    daily_duration.toString(),
//                    daily_heartPoints.toString(),
//                    fiveDays_distance.toString(),
//                    fiveDays_calories.toString()
//                )
//            }
//            periodical.toLowerCase() == "weekly" -> {
//                weeklyRecord()
//
//                writeNewPost(
//                    user_id,
//                    weekly_steps.toString(),
//                    daily_duration.toString(),
//                    daily_heartPoints.toString(),
//                    weekly_distance.toString(),
//                    weekly_calories.toString()
//                )
//            }
//        }
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
