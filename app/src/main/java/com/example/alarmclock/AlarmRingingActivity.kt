package com.example.alarmclock

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmRingingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure activity shows over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        setContent {
            CyberpunkTheme {
                RingingScreen(onStop = { stopAlarm() })
            }
        }
    }

    private fun stopAlarm() {
        // Stop the service
        stopService(Intent(this, AlarmService::class.java))
        
        // Remove notifications
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(2) // Trigger notification ID
        notificationManager.cancel(MainActivity.NOTIFICATION_ID)
        
        finish()
    }
}

@Composable
fun RingingScreen(onStop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ALARM BREACH",
            style = TextStyle(
                color = Color.Red,
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 100.dp)
        )

        Button(
            onClick = onStop,
            modifier = Modifier
                .width(280.dp)
                .height(110.dp)
                .shadow(
                    elevation = 40.dp,
                    shape = RoundedCornerShape(55.dp),
                    spotColor = Color(0xFF00FF00),
                    ambientColor = Color(0xFF00FF00)
                )
                .border(4.dp, Color(0xFF00FF00), RoundedCornerShape(55.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color(0xFF00FF00)
            ),
            shape = RoundedCornerShape(55.dp)
        ) {
            Text(
                "STOP ALARM",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
