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

        val textView = findViewById<TextView>(android.R.id.content)

        showShiftInfo()
    }

    private fun scheduleAllShiftAlarms() {

        val alarmManager =
            getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()

        // Shift cycle starts from Saturday, 18 July 2026
        val startDate = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 18, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Schedule alarms for the next 90 days
        for (day in 0 until 90) {

            val date = Calendar.getInstance().apply {
                timeInMillis = startDate.timeInMillis
                add(Calendar.DAY_OF_YEAR, day)
            }

            val dayOfCycle = day % 21

            // Friday is weekly off.
            // Cycle:
            // 6 Morning + Friday Off
            // 6 Night + Friday Off
            // 6 Afternoon + Friday Off

            val shiftType: String
            val alarmHour: Int

            when {
                dayOfCycle in 0..5 -> {
                    shiftType = "Morning Shift"
                    alarmHour = 5
                }

                dayOfCycle == 6 -> {
                    continue
                }

                dayOfCycle in 7..12 -> {
                    shiftType = "Night Shift"
                    alarmHour = 20
                }

                dayOfCycle == 13 -> {
                    continue
                }

                dayOfCycle in 14..19 -> {
                    shiftType = "Afternoon Shift"
                    alarmHour = 11
                }

                else -> {
                    continue
                }
            }

            if (shiftType == "Morning Shift") {

                for ((hour, minute) in alarmTimes) {

                    val alarmDate = Calendar.getInstance().apply {
                        timeInMillis = date.timeInMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    scheduleAlarm(
                        alarmManager,
                        alarmDate,
                        "$shiftType - Wake Up"
                    )
                }

            } else {

                val alarmDate = Calendar.getInstance().apply {
                    timeInMillis = date.timeInMillis
                    set(Calendar.HOUR_OF_DAY, alarmHour)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                scheduleAlarm(
                    alarmManager,
                    alarmDate,
                    "$shiftType - Wake Up"
                )
            }
        }
    }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        calendar: Calendar,
        message: String
    ) {

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("message", message)
        }

        val requestCode =
            calendar.timeInMillis.toInt()

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun showShiftInfo() {

        val today = Calendar.getInstance()

        val startDate = Calendar.getInstance().apply {
            set(2026, Calendar.JULY, 18, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val daysPassed =
            ((today.timeInMillis - startDate.timeInMillis)
                    / (24 * 60 * 60 * 1000)).toInt()

        val cycleDay =
            if (daysPassed >= 0) daysPassed % 21 else 0

        val shift = when {
            cycleDay in 0..5 -> "Morning Shift\nWake up alarms: 4:50, 5:00, 5:15, 5:30 AM"
            cycleDay == 6 -> "WEEK OFF"
            cycleDay in 7..12 -> "Night Shift\nWake up: 8:00 PM"
            cycleDay == 13 -> "WEEK OFF"
            cycleDay in 14..19 -> "Afternoon Shift\nWake up: 11:00 AM"
            else -> "WEEK OFF"
        }

        println("Today's Shift: $shift")
    }
}
