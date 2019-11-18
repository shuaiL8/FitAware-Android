package com.vt.fitaware.FirebaseMessagingService;

import android.app.AlertDialog
import android.content.*
import android.os.Build
import android.util.Log


class MyReceiver : BroadcastReceiver() {
        private val TAG = "MyBroadcastReceiver"

        override fun onReceive(context: Context, intent: Intent) {
                val message = intent.getStringExtra("message")
                Log.i(TAG, "BroadcastReceiver message: $message")



                if(!message.isNullOrEmpty()) {
                        Log.i(TAG, "notification: $message")
                        val dialogBuilder = AlertDialog.Builder(context)

                        dialogBuilder
                                .setTitle("FitAware")
                                .setMessage(message)
                                .setPositiveButton("Ok", DialogInterface.OnClickListener {
                                                dialog, id ->
                                })
                        val alert = dialogBuilder.create()

                        if (Build.VERSION.SDK_INT >= 26) {
                                alert.show()
                        }
                }

        }
}