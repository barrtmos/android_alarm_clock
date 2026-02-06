package com.example.alarmclock

import android.app.KeyguardManager
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource


class AlarmRingingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOn()
        
        setContent {
            CyberpunkTheme {
                RingingScreen(onStop = { stopAlarm() })
            }
        }
    }

    private fun turnScreenOn() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        // Flags from decompiled app: FLAG_TURN_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED | FLAG_KEEP_SCREEN_ON | FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        // Value: 2621569 (0x280081)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    private fun stopAlarm() {
        // Stop the service
        stopService(Intent(this, AlarmService::class.java))
        
        // Remove notifications
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(2) // Trigger notification ID
        notificationManager.cancel(MainActivity.NOTIFICATION_ID)
        
        finishAffinity()
    }
}

@Composable
fun RingingScreen(onStop: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_alarm),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x7A003D45))
        )

        Column(
            modifier = Modifier
                .fillMaxSize(),
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
}
