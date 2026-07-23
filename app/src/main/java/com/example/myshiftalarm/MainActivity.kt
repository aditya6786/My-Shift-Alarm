package com.example.myshiftalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val alarmTimes = arrayOf(
        4 to 50,
        5 to 0,
        5 to 15,
        5 to 30
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduleAllShiftAlarms()
        showShiftInfo()
    }

    private fun scheduleAllShiftAlarms() {

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Shift cycle starts from Saturday, 18 July 2026
        val startDate = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 18, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Schedule alarms for next 90 days
        for (day in 0 until 90) {

            val date = Calendar.getInstance().apply {
                timeInMillis = startDate.timeInMillis
                add(Calendar.DAY_OF_YEAR, day)
            }

            val dayOfCycle = day % 21

            // Weekly off: every 7th day
            if (dayOfCycle == 6 ||
                dayOfCycle == 13 ||
                dayOfCycle == 20
            ) {
                continue
            }

            val shiftName: String
            val alarmHour: Int
            val alarmMinute: Int

            when {
                dayOfCycle < 7 -> {
                    shiftName = "Morning Shift"
                    alarmHour = 4
                    alarmMinute = 50
                }

                dayOfCycle < 14 -> {
                    shiftName = "Afternoon Shift"
                    alarmHour = 12
                    alarmMinute = 50
                }

                else -> {
                    shiftName = "Night Shift"
                    alarmHour = 20
                    alarmMinute = 50
                }
            }

            val alarmCalendar = Calendar.getInstance().apply {
                timeInMillis = date.timeInMillis
                set(Calendar.HOUR_OF_DAY, alarmHour)
                set(Calendar.MINUTE, alarmMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (alarmCalendar.timeInMillis > System.currentTimeMillis()) {

                val intent = Intent(this, AlarmReceiver::class.java).apply {
                    putExtra("shift_name", shiftName)
                }

                val requestCode = day * 100 + alarmHour * 10 + alarmMinute

                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmCalendar.timeInMillis,
                        pendingIntent
                    )
                } catch (e: SecurityException) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmCalendar.timeInMillis,
                        pendingIntent
                    )
                }
            }
        }
    }

    private fun showShiftInfo() {

        val currentShiftText =
            findViewById<TextView>(R.id.currentShiftText)

        val shiftDetailsText =
            findViewById<TextView>(R.id.shiftDetailsText)

        val startDate = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 18, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance()

        val daysSinceStart =
            ((today.timeInMillis - startDate.timeInMillis)
                    / (24 * 60 * 60 * 1000)).toInt()

        if (daysSinceStart < 0) {

            currentShiftText.text = "Shift Cycle Not Started"

            shiftDetailsText.text =
                "Shift cycle starts on 18 July 2026"

            return
        }

        val dayOfCycle = daysSinceStart % 21

        if (dayOfCycle == 6 ||
            dayOfCycle == 13 ||
            dayOfCycle == 20
        ) {

            currentShiftText.text = "Weekly Off"

            shiftDetailsText.text =
                "Today is your weekly off."

            return
        }

        val shiftName = when {
            dayOfCycle < 7 -> "Morning Shift"
            dayOfCycle < 14 -> "Afternoon Shift"
            else -> "Night Shift"
        }

        val dateFormat =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        currentShiftText.text = shiftName

        shiftDetailsText.text =
            "Date: ${dateFormat.format(today.time)}\n" +
            "Day ${dayOfCycle + 1} of 21-day cycle\n\n" +
            "Your alarm has been scheduled."
    }
}
