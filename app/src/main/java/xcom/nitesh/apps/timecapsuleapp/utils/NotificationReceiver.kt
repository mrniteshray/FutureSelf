package xcom.nitesh.apps.timecapsuleapp.utils

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcom.nitesh.apps.timecapsuleapp.R
import xcom.nitesh.apps.timecapsuleapp.retrofit.EmailRequest
import xcom.nitesh.apps.timecapsuleapp.retrofit.Recipient
import xcom.nitesh.apps.timecapsuleapp.retrofit.RetrofitInstance
import xcom.nitesh.apps.timecapsuleapp.retrofit.Sender
import xcom.nitesh.apps.timecapsuleapp.ui.display.DetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "No Title"
        val message = intent?.getStringExtra("message") ?: "No Message"
        val actualmsg = intent?.getStringExtra("actualmsg") ?: "No Message"
        val toEmail = intent?.getStringExtra("toEmail") ?: "No Email"

        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("title", title)
            putExtra("message", message)
        }

        sendEmail(context,toEmail, message,actualmsg)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, "capsule_channel")
            .setContentTitle(title)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun sendEmail(context: Context, toEmail: String, subject: String, body: String) {
        val emailRequest = EmailRequest(
            sender = Sender("niteshr070104@gmail.com", "FutureSelf"), // Replace with your email
            to = listOf(Recipient(toEmail, "User")),
            subject = subject,
            textContent = body
        )

        CoroutineScope(Dispatchers.IO).launch {
            RetrofitInstance.api.sendEmail(emailRequest).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Email sent to $toEmail", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to send email", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }
    }
}
