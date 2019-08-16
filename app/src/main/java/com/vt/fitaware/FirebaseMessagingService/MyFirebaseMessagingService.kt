package com.vt.fitaware.FirebaseMessagingService

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
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.database.FirebaseDatabase
import com.vt.fitaware.MainActivity
import com.vt.fitaware.R
import java.text.SimpleDateFormat
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMessagingService"
    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"
    private var my_goal: String = "0"
    private var my_rank: String = "0"

    private var my_steps: String = "0"
    private var my_duration: String = "0"
    private var my_heartPoints: String = "0"
    private var my_calories: String = "0"
    private var my_distance: String = "0"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        user_id = sharedPreferences!!.getString("user_id", "none")
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
            val data = remoteMessage.data
            if (data != null) {
                handleNow(data)
            }
            

            val intent = Intent()
            intent.action = "com.vt.BroadcastReceiver"
            intent.putExtra("message", remoteMessage.data["message"])
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES

            sendBroadcast(intent)

        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)

            sendNotification(remoteMessage)

            val intent = Intent()
            intent.action = "com.vt.BroadcastReceiver"
            intent.putExtra("message", remoteMessage.notification!!.body!!)
            intent.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES

            sendBroadcast(intent)
            Log.d(TAG, "intent message: " + intent.extras)

        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, "Regular Notification")
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_steps)
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

    private fun handleNow(data: Map<String, String>) {

        val CHANNEL_ID = "my_channel_like"

        if(Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Like",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle(data["title"])
            .setContentText(data["message"])
            .setAutoCancel(true)
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)


        Log.d(TAG, "Notification sent: " + data["message"])


    }

    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance().beginWith(work).enqueue()

    }

    private fun sendRegistrationToServer(token: String?) {


    }
}
