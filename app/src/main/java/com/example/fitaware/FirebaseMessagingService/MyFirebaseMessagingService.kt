package com.example.fitaware.FirebaseMessagingService

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.fitaware.MainActivity
import com.example.fitaware.R


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingService"
    private var sharedPreferences: SharedPreferences? = null

    private var my_steps: String = "0"
    private var my_goal: String = "0"
    private var my_rank: String = "0"

    private var my_duration: String = "0"
    private var my_heartPoints: String = "0"
    private var my_calories: String = "0"
    private var my_distance: String = "0"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        my_goal = sharedPreferences!!.getString("my_goal", "0")

        my_rank = sharedPreferences!!.getString("rank", "0")
        my_steps = sharedPreferences!!.getString("currentSteps", "0")
        my_duration = sharedPreferences!!.getString("duration", "0")
        my_heartPoints = sharedPreferences!!.getString("heartPoints", "0")
        my_distance = sharedPreferences!!.getString("distance", "0")
        my_calories = sharedPreferences!!.getString("calories", "0")

        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            scheduleJob()

            // Handle message within 10 seconds
            handleNow()

        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
            if(remoteMessage.notification!!.body!! == "Here are your total steps today") {
                sendNotification(remoteMessage, my_steps)
            }
            else {
                sendNotification(remoteMessage, "test")
            }

            val intent = Intent()
            intent.action = "BroadcastReceiver"
            intent.putExtra("message", remoteMessage.notification!!.body!!)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES

            sendBroadcast(intent)
            Log.d(TAG, "intent message: " + intent.extras)

        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage, steps: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
            .setContentText(remoteMessage.notification?.body + steps)
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun handleNow() {

    }

    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance().beginWith(work).enqueue()

    }

    private fun sendRegistrationToServer(token: String?) {


    }
}
