package com.vt.fitaware

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import androidx.work.Operation.State.SUCCESS
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.RemoteMessage


class MyReportWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    private val TAG = "MyReportWorker"

    private var sharedPreferences: SharedPreferences? = null

    private var user_id: String = "none"


    override fun doWork(): Result {

        initSharedPreferences()

        // initial all the values
        user_id = sharedPreferences!!.getString("user_id", "none")


        return try {
            Log.d(TAG, "doWork: work is done")
            Log.d(TAG, "user_id: $user_id")

            sendNotification(user_id)

            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }

    private fun sendNotification(message: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 3 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, "Report Notification")
            .setContentTitle("Progress Report")
            .setContentText(message)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_steps)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(3 /* ID of notification */, notificationBuilder.build())
    }

    private fun initSharedPreferences() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }
}