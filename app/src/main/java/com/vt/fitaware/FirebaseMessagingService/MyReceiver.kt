package com.example.fitaware.FirebaseMessagingService;

import android.app.AlertDialog
import android.content.*
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class MyReceiver : BroadcastReceiver() {
        private val TAG = "MyBroadcastReceiver"
        private var sharedPreferences: SharedPreferences? = null
        private var user_id: String = ""

        private var my_steps: String = "0"
        private var my_goal: String = "0"
        private var my_rank: String = "0"
        private var my_duration: String = "0"
        private var my_heartPoints: String = "0"
        private var my_calories: String = "0"
        private var my_distance: String = "0"

        override fun onReceive(context: Context, intent: Intent) {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

                user_id = sharedPreferences!!.getString("user_id", "")

                my_goal = sharedPreferences!!.getString("my_goal", "0")
                my_rank = sharedPreferences!!.getString("rank", "0")
                my_steps = sharedPreferences!!.getString("currentSteps", "0")
                my_duration = sharedPreferences!!.getString("duration", "0")
                my_heartPoints = sharedPreferences!!.getString("heartPoints", "0")
                my_distance = sharedPreferences!!.getString("distance", "0")
                my_calories = sharedPreferences!!.getString("calories", "0")

                Log.d(TAG, "BroadcastReceiver intent: " + intent.extras)

                Log.i(TAG, "sharedPreferences my_steps: $my_steps")
                Log.i(TAG, "sharedPreferences my_duration: $my_duration")
                Log.i(TAG, "sharedPreferences my_heartPoints: $my_heartPoints")
                Log.i(TAG, "sharedPreferences my_distance: $my_distance")
                Log.i(TAG, "sharedPreferences my_calories: $my_calories")

                val message = intent.getStringExtra("message")
                Log.i(TAG, "BroadcastReceiver message: $message")


                val dialogBuilder = AlertDialog.Builder(context)

                if(!message.isNullOrEmpty()) {
                        Log.i(TAG, "notification: $message")
                        if(message == "Here are your total steps today") {
                                recordDaily(user_id, my_calories, my_goal, my_heartPoints, my_duration, my_distance, my_rank, my_steps)
                                resetPost(user_id)

                                dialogBuilder
                                        .setTitle("FitAware")
                                        .setMessage("$message: $my_steps steps")
                                        .setPositiveButton("Ok", DialogInterface.OnClickListener {
                                                        dialog, id ->
                                        })
                        }
                        else {
                                dialogBuilder
                                        .setTitle("FitAware")
                                        .setMessage(message)
                                        .setPositiveButton("Ok", DialogInterface.OnClickListener {
                                                        dialog, id ->
                                        })
                        }


                        val alert = dialogBuilder.create()
                        alert.show()
                }

        }

        private fun recordDaily(id: String, Cals: String, Goal: String, HPs: String, Minis: String, Ms: String, Rank: String, Steps: String) {
                val calendar = Calendar.getInstance()
                val mdformat = SimpleDateFormat("yyyy-MM-dd")
                val strDate = mdformat.format(calendar.time)

                val childUpdates = HashMap<String, Any>()
                childUpdates["/DailyRecord/$id/$strDate/Cals"] = Cals
                childUpdates["/DailyRecord/$id/$strDate/Goal"] = Goal
                childUpdates["/DailyRecord/$id/$strDate/HPs"] = HPs
                childUpdates["/DailyRecord/$id/$strDate/Cals"] = Cals
                childUpdates["/DailyRecord/$id/$strDate/Minis"] = Minis
                childUpdates["/DailyRecord/$id/$strDate/Ms"] = Ms
                childUpdates["/DailyRecord/$id/$strDate/Rank"] = Rank
                childUpdates["/DailyRecord/$id/$strDate/Steps"] = Steps

                Log.w(TAG, "childUpdates: $childUpdates")

                FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)

        }

        private fun resetPost(id: String) {
                val childUpdates = HashMap<String, Any>()
                val editor = sharedPreferences?.edit()

                childUpdates["/User/$id/currentSteps"] = "0"
                childUpdates["/User/$id/duration"] = "0"
                childUpdates["/User/$id/heartPoints"] = "0"
                childUpdates["/User/$id/distance"] = "0"
                childUpdates["/User/$id/calories"] = "0"

                editor!!.putString("calories", "0")
                editor!!.putString("distance", "0")
                editor!!.putString("heartPoints", "0")
                editor!!.putString("duration", "0")
                editor!!.putString("currentSteps", "0")
                editor.commit()


                Log.w(TAG, "childUpdates: $childUpdates")

                FirebaseDatabase.getInstance().reference.updateChildren(childUpdates)
        }
}