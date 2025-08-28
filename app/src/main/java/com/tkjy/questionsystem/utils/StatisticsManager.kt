package com.tkjy.questionsystem.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tkjy.questionsystem.models.StudySession
import java.util.*

data class DailyStatistics(
    val date: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalStudyTime: Long,
    val sessionsCount: Int
) {
    val accuracyRate: Double
        get() = if (totalQuestions > 0) (correctAnswers.toDouble() / totalQuestions) * 100 else 0.0
}

class StatisticsManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("statistics", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun saveStudySession(session: StudySession) {
        val sessions = getAllSessions().toMutableList()
        sessions.add(session)
        
        val sessionsJson = gson.toJson(sessions)
        prefs.edit().putString("study_sessions", sessionsJson).apply()
    }
    
    fun getAllSessions(): List<StudySession> {
        val sessionsJson = prefs.getString("study_sessions", null)
        return if (sessionsJson != null) {
            val type = object : TypeToken<List<StudySession>>() {}.type
            gson.fromJson(sessionsJson, type)
        } else {
            emptyList()
        }
    }
    
    fun getTodayStatistics(): DailyStatistics {
        val today = getTodayDateString()
        val todaySessions = getAllSessions().filter { 
            val sessionDate = getDateString(it.startTime)
            sessionDate == today
        }
        
        val totalQuestions = todaySessions.sumOf { it.answeredQuestions }
        val correctAnswers = todaySessions.sumOf { it.correctAnswers }
        val totalTime = todaySessions.sumOf { it.getDuration() }
        
        return DailyStatistics(
            date = today,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            totalStudyTime = totalTime,
            sessionsCount = todaySessions.size
        )
    }
    
    fun getWeeklyStatistics(): List<DailyStatistics> {
        val calendar = Calendar.getInstance()
        val weekStats = mutableListOf<DailyStatistics>()
        
        // Get last 7 days
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = getDateString(calendar.timeInMillis)
            
            val daySessions = getAllSessions().filter { 
                getDateString(it.startTime) == dateString
            }
            
            val totalQuestions = daySessions.sumOf { it.answeredQuestions }
            val correctAnswers = daySessions.sumOf { it.correctAnswers }
            val totalTime = daySessions.sumOf { it.getDuration() }
            
            weekStats.add(
                DailyStatistics(
                    date = dateString,
                    totalQuestions = totalQuestions,
                    correctAnswers = correctAnswers,
                    totalStudyTime = totalTime,
                    sessionsCount = daySessions.size
                )
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, i) // Reset for next iteration
        }
        
        return weekStats
    }
    
    fun getOverallStatistics(): Map<String, Any> {
        val allSessions = getAllSessions()
        
        val totalQuestions = allSessions.sumOf { it.answeredQuestions }
        val totalCorrect = allSessions.sumOf { it.correctAnswers }
        val totalTime = allSessions.sumOf { it.getDuration() }
        val totalSessions = allSessions.size
        
        val accuracyRate = if (totalQuestions > 0) {
            (totalCorrect.toDouble() / totalQuestions) * 100
        } else 0.0
        
        val averageSessionTime = if (totalSessions > 0) {
            totalTime / totalSessions
        } else 0L
        
        return mapOf(
            "totalQuestions" to totalQuestions,
            "totalCorrect" to totalCorrect,
            "totalTime" to totalTime,
            "totalSessions" to totalSessions,
            "accuracyRate" to accuracyRate,
            "averageSessionTime" to averageSessionTime
        )
    }
    
    fun getCategoryStatistics(): Map<String, DailyStatistics> {
        val allSessions = getAllSessions()
        val categoryStats = mutableMapOf<String, MutableList<StudySession>>()
        
        // Group sessions by category
        allSessions.forEach { session ->
            val category = session.category.ifEmpty { "默认" }
            categoryStats.getOrPut(category) { mutableListOf() }.add(session)
        }
        
        // Calculate stats for each category
        return categoryStats.mapValues { (category, sessions) ->
            val totalQuestions = sessions.sumOf { it.answeredQuestions }
            val correctAnswers = sessions.sumOf { it.correctAnswers }
            val totalTime = sessions.sumOf { it.getDuration() }
            
            DailyStatistics(
                date = category,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                totalStudyTime = totalTime,
                sessionsCount = sessions.size
            )
        }
    }
    
    private fun getTodayDateString(): String {
        return getDateString(System.currentTimeMillis())
    }
    
    private fun getDateString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}