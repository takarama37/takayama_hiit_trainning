package com.example.takayama_hiit_training

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HiitApp()
        }
    }
}

@Composable
fun HiitApp() {

    val context = LocalContext.current

    // ✅ 安全に保持
    val prefs = remember {
        context.getSharedPreferences("Hiit", Context.MODE_PRIVATE)
    }

    var workTime by remember { mutableStateOf(prefs.getInt("work", 30).toString()) }
    var restTime by remember { mutableStateOf(prefs.getInt("rest", 10).toString()) }
    var rounds by remember { mutableStateOf(prefs.getInt("rounds", 5).toString()) }

    var currentTime by remember { mutableStateOf(0L) }
    var currentRound by remember { mutableStateOf(1) }
    var isWorking by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("停止中") }

    var timer: CountDownTimer? by remember { mutableStateOf(null) }

    // 🔊 完了音（システム音）
    val mediaPlayer = remember {
        MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
    }

    fun startTimer() {
        val work = workTime.toLongOrNull() ?: 30
        val rest = restTime.toLongOrNull() ?: 10
        val totalRounds = rounds.toIntOrNull() ?: 5

        currentRound = 1
        isWorking = true

        fun runPhase() {
            val duration = if (isWorking) work else rest

            timer = object : CountDownTimer(duration * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    currentTime = millisUntilFinished / 1000
                    status = if (isWorking) "運動中" else "休憩中"
                }

                override fun onFinish() {

                    // 🔊 フェーズ切替音
                    mediaPlayer.start()

                    if (!isWorking) currentRound++

                    if (currentRound > totalRounds) {
                        status = "完了！"
                        mediaPlayer.start() // 最後も鳴らす
                    } else {
                        isWorking = !isWorking
                        runPhase()
                    }
                }
            }.start()
        }

        runPhase()
    }

    fun stopTimer() {
        timer?.cancel()
        status = "停止中"
    }

    fun save() {
        prefs.edit()
            .putInt("work", workTime.toIntOrNull() ?: 30)
            .putInt("rest", restTime.toIntOrNull() ?: 10)
            .putInt("rounds", rounds.toIntOrNull() ?: 5)
            .apply()
    }

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = workTime,
            onValueChange = { workTime = it },
            label = { Text("運動時間（秒）") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = restTime,
            onValueChange = { restTime = it },
            label = { Text("休憩時間（秒）") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = rounds,
            onValueChange = { rounds = it },
            label = { Text("回数") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { startTimer() }) {
            Text("スタート")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { stopTimer() }) {
            Text("停止")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { save() }) {
            Text("保存")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("状態：$status")
        Text("残り：$currentTime 秒")
        Text("ラウンド：$currentRound")
    }
}