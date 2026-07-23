android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val channelId = "SHIFT_ALARM_CHANNEL"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Shift Alarm",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationManager.createNotificationChannel(channel)

        val message = intent.getStringExtra("message")
            ?: "Shift Alarm"

        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("My Shift Alarm")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        
    }
            }
