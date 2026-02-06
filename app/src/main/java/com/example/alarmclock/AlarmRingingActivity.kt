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
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay


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
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                        radius = 1000f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x3D003D45))
        )

        var elapsedSeconds by remember { mutableStateOf(0) }
        val alphaAnimatable = remember { Animatable(0f) }
        val scaleAnimatable = remember { Animatable(0.5f) }

        LaunchedEffect(Unit) {
            // Start counter with 2s interval
            while(true) {
                delay(2000)
                elapsedSeconds += 2
                // Reset scale and animate to 1f
                scaleAnimatable.snapTo(0.0f) 
                scaleAnimatable.animateTo(
                    targetValue = 1f, 
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }

        LaunchedEffect(Unit) {
            // Fade in after 1.5 seconds
            delay(1500)
            alphaAnimatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.33f))
            
            Text(
                text = "ALARM BREACH",
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 40.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .shadow(elevation = 20.dp, spotColor = Color.Red)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Timer Text
            Text(
                text = "$elapsedSeconds",
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 96.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Red,
                        blurRadius = 30f
                    )
                ),
                modifier = Modifier
                    .graphicsLayer(
                        alpha = alphaAnimatable.value,
                        scaleX = scaleAnimatable.value,
                        scaleY = scaleAnimatable.value
                    )
            )

            Spacer(modifier = Modifier.fillMaxHeight(0.15f))

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
                    .border(3.dp, Color(0xFF00FF00).copy(alpha = 0.25f), RoundedCornerShape(55.dp)),
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
