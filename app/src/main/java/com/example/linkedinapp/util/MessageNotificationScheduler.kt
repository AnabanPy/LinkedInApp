package com.example.linkedinapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.linkedinapp.worker.MessageCheckWorker
import java.util.concurrent.TimeUnit

object MessageNotificationScheduler {
    private const val WORK_NAME = "message_check_work"
    
    /**
     * Запускает периодическую проверку новых сообщений
     */
    fun startPeriodicCheck(context: Context, userId: Long) {
        // Сохраняем ID пользователя для Worker
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("current_user_id", userId).apply()
        
        // Создаем ограничения для работы
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Требуется интернет
            .build()
        
        // Создаем периодическую работу (каждые 15 минут минимум, но можем указать больше)
        // WorkManager требует минимум 15 минут для периодических задач
        val workRequest = PeriodicWorkRequestBuilder<MessageCheckWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        // Запускаем работу (если уже существует, заменяем)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE, // Заменяем существующую работу для обновления
            workRequest
        )
    }
    
    /**
     * Останавливает периодическую проверку
     */
    fun stopPeriodicCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        
        // Очищаем сохраненный ID пользователя
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("current_user_id").apply()
    }
    
    /**
     * Обновляет ID пользователя для Worker
     */
    fun updateUserId(context: Context, userId: Long) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("current_user_id", userId).apply()
    }
}

