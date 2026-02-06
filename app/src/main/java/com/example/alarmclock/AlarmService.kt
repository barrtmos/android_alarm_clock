package com.example.alarmclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "CyberAlarm::WakeLock")
        wakeLock?.acquire(20 * 60 * 1000L /* 20 minutes */)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("CyberAlarm", "AlarmService: onStartCommand triggered with action: ${intent?.action}")

        if (intent?.action == "STOP_ALARM") {
            stopSelf()
            return START_NOT_STICKY
        }

        // 1. Create Notification and start Foreground IMMEDIATELY
        val notification = createAlarmNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(2, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(2, notification)
        }

        // 2. Start Audio and Vibration
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            Log.d("CyberAlarm", "AlarmService: MediaPlayer already playing")
            return START_STICKY
        }
        
        startVibration()

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound).apply {
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                setVolume(1.0f, 1.0f)
                start()
            }
            Log.d("CyberAlarm", "AlarmService: Custom melody started")
        } catch (e: Exception) {
            Log.e("CyberAlarm", "AlarmService: Error starting MediaPlayer with custom sound", e)
        }

        return START_STICKY
    }

    private fun createAlarmNotification(): android.app.Notification {
        val channelId = "alarm_trigger_channel_v3"
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)

        // Intent for the ringing activity
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("intent.extra.alarm", 1) // Mimic reference app
        }
        val fullScreenPendingIntent = android.app.PendingIntent.getActivity(
            this, 1, fullScreenIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action Intent
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM"
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 1, stopIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alarm Trigger",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null) 
                enableVibration(false) // Decompiled app sets vibrate to [0], i.e. no vibration from notification itself
                description = "Critical Alarm Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        return androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("SYSTEM BREACH")
            .setContentText("ALARM TRIGGERED - ACTION REQUIRED")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH) // Reference uses 1 (HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
            .setLocalOnly(true) // From Reference
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_notification, "STOP", stopPendingIntent)
            .setSound(null)    // From Reference
            .setVibrate(longArrayOf(0)) // From Reference
            .build()
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 1000, 500, 1000), 0)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 1000, 500, 1000), 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
