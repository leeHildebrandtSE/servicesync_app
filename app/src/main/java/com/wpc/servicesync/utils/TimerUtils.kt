// File: app/src/main/java/com/wpc/servicesync/utils/TimerUtils.kt
package com.wpc.servicesync.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.TimeUnit

class TimerUtils {
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var isRunning = false
    private var isPaused = false
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var onTickListener: ((String) -> Unit)? = null

    fun startTimer(onTick: (String) -> Unit) {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            isPaused = false
            onTickListener = onTick

            timerRunnable = object : Runnable {
                override fun run() {
                    if (isRunning && !isPaused) {
                        elapsedTime = System.currentTimeMillis() - startTime
                        onTickListener?.invoke(formatTimeFromMillis(elapsedTime))
                        handler.postDelayed(this, 1000)
                    }
                }
            }
            handler.post(timerRunnable!!)
        }
    }

    fun pauseTimer() {
        isPaused = true
    }

    fun resumeTimer() {
        if (isRunning && isPaused) {
            startTime = System.currentTimeMillis() - elapsedTime
            isPaused = false
            timerRunnable?.let { handler.post(it) }
        }
    }

    fun stopTimer() {
        isRunning = false
        isPaused = false
        handler.removeCallbacks(timerRunnable ?: return)
    }

    fun resetTimer() {
        stopTimer()
        elapsedTime = 0
    }

    fun getElapsedTime(): Long {
        return elapsedTime
    }

    private fun formatTimeFromMillis(millis: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        return formatTimeFromSeconds(seconds)
    }

    companion object {
        fun formatTimeFromSeconds(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60

            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format("%02d:%02d", minutes, secs)
            }
        }

        fun formatTime(millis: Long): String {
            val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
            return formatTimeFromSeconds(seconds)
        }
    }
}