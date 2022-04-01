package com.hundredstartups.openvpn.firebase

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hundredstartups.openvpn.view.MainActivity
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        sendNotification(remoteMessage)
        if (remoteMessage.notification != null) {
            Log.d("TAG", "Message Notification Body: " + remoteMessage.notification!!.body)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage?) {
        if (remoteMessage != null) {
            remoteMessage.data
            var message: String? = ""
            if (remoteMessage.data.containsKey("message")) {
                message = remoteMessage.data["message"]
            } else if (remoteMessage.data["body"] != null) {
                message = remoteMessage.data["body"]
            }
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val builder: NotificationCompat.Builder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                    val notificationChannel = NotificationChannel("ID", "Name", importance)
                    notificationManager.createNotificationChannel(notificationChannel)
                    NotificationCompat.Builder(applicationContext, notificationChannel.id)
                } else {
                    NotificationCompat.Builder(applicationContext)
                }
            val defaultSoundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            var notificationTitle = ""
            builder
                .setSmallIcon(R.drawable.ic_popup_reminder)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
            notificationManager.notify(Random().nextInt(), builder.build())
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        sendRegistrationToServer(s)
    }

    private fun sendRegistrationToServer(token: String) {

    }

    companion object {
        const val TAG_MESSAGE = "message"
        const val TAG_NOTIFY = "notify"
    }
}