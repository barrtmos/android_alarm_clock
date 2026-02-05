package com.example.alarmclock

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "alarm_status_channel_v3"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
        setContent {
            CyberpunkTheme {
                CyberpunkScreen(onSetAlarm = { h, m -> scheduleAlarm(h, m) })
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Status"
            val descriptionText = "Shows active alarm status"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleAlarm(hours: Int, minutes: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        // Explicitly target our receiver
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = "com.example.alarmclock.ACTION_ALARM"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        val diff = calendar.timeInMillis - System.currentTimeMillis()
        Log.i("CyberAlarm", "ALARM SET FOR: ${calendar.time} (in ${diff / 1000} seconds)")
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        showStatusNotification(hours, minutes)
        finish()
    }

    private fun showStatusNotification(hours: Int, minutes: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("ALARM ENGAGED")
            .setContentText("Target time: %02d:%02d".format(hours, minutes))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

@Composable
fun CyberpunkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color.Black,
            surface = Color.Black,
            primary = Color(0xFF00FF00),
            onPrimary = Color.Black,
            onBackground = Color(0xFF00FF00),
            onSurface = Color(0xFF00FF00)
        ),
        content = content
    )
}

@Composable
fun CyberpunkScreen(onSetAlarm: (Int, Int) -> Unit) {
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while(true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SYSTEM CLOCK: $currentTime",
            style = TextStyle(
                color = Color(0xFF00FF00),
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "CYBER ALARM",
            style = TextStyle(
                color = Color(0xFF00FF00),
                fontSize = 42.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .padding(bottom = 8.dp)
                .shadow(elevation = 15.dp, shape = RectangleShape, ambientColor = Color(0xFF00FF00), spotColor = Color(0xFF00FF00))
        )

        Text(
            text = "!!! USE 24-HOUR FORMAT !!!",
            style = TextStyle(
                color = Color.Red,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Time Input Row (removed outer container)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            NeonInput(
                value = hours,
                onValueChange = { if (it.length <= 2) hours = it.filter { it.isDigit() } },
                label = "HH",
                modifier = Modifier.size(width = 80.dp, height = 90.dp)
            )
            Text(
                ":",
                color = Color(0xFF00FF00),
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            NeonInput(
                value = minutes,
                onValueChange = { if (it.length <= 2) minutes = it.filter { it.isDigit() } },
                label = "MM",
                modifier = Modifier.size(width = 80.dp, height = 90.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val h = hours.toIntOrNull() ?: 0
                val m = minutes.toIntOrNull() ?: 0
                onSetAlarm(h % 24, m % 60)
            },
            modifier = Modifier
                .width(300.dp)
                .height(70.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0xFF00FF00),
                    ambientColor = Color(0xFF00FF00)
                )
                .border(2.dp, Color(0xFF00FF00), RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color(0xFF00FF00)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                "ARM SYSTEM",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeonInput(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF00FF00)
            )
            .background(Color.Black, RoundedCornerShape(24.dp))
            .border(3.dp, Color(0xFF00FF00).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
    ) {
        // Label in top-left
        Text(
            text = label,
            color = Color(0xFF00FF00).copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
        )

        // Clean input field
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = Color(0xFF00FF00),
                fontSize = 32.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00FF00)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(top = 16.dp), // Space for label
            singleLine = true
        )
    }
}
