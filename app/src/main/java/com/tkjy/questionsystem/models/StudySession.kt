package com.tkjy.questionsystem.models

import com.google.gson.annotations.SerializedName

data class StudySession(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("mode")
    val mode: String = "", // memory, practice, test, review
    
    @SerializedName("category")
    val category: String = "",
    
    @SerializedName("startTime")
    val startTime: Long = System.currentTimeMillis(),
    
    @SerializedName("endTime")
    var endTime: Long = 0L,
    
    @SerializedName("totalQuestions")
    var totalQuestions: Int = 0,
    
    @SerializedName("answeredQuestions")
    var answeredQuestions: Int = 0,
    
    @SerializedName("correctAnswers")
    var correctAnswers: Int = 0,
    
    @SerializedName("skippedQuestions")
    var skippedQuestions: Int = 0,
    
    @SerializedName("currentQuestionIndex")
    var currentQuestionIndex: Int = 0,
    
    @SerializedName("questions")
    val questions: MutableList<Question> = mutableListOf(),
    
    @SerializedName("userAnswers")
    val userAnswers: MutableMap<Int, String> = mutableMapOf(), // questionId -> answer
    
    @SerializedName("questionResults")
    val questionResults: MutableMap<Int, Boolean> = mutableMapOf(), // questionId -> correct
    
    @SerializedName("timeSpentPerQuestion")
    val timeSpentPerQuestion: MutableMap<Int, Long> = mutableMapOf(), // questionId -> time in ms
    
    @SerializedName("paused")
    var paused: Boolean = false,
    
    @SerializedName("pausedTime")
    var pausedTime: Long = 0L,
    
    @SerializedName("totalPausedDuration")
    var totalPausedDuration: Long = 0L
) {
    
    fun getDuration(): Long {
        val end = if (endTime > 0) endTime else System.currentTimeMillis()
        return end - startTime - totalPausedDuration
    }
    
    fun getAccuracyRate(): Double {
        return if (answeredQuestions > 0) {
            (correctAnswers.toDouble() / answeredQuestions.toDouble()) * 100
        } else {
            0.0
        }
    }
    
    fun getProgress(): Double {
        return if (totalQuestions > 0) {
            (currentQuestionIndex.toDouble() / totalQuestions.toDouble()) * 100
        } else {
            0.0
        }
    }
    
    fun getCurrentQuestion(): Question? {
        return if (currentQuestionIndex < questions.size) {
            questions[currentQuestionIndex]
        } else {
            null
        }
    }
    
    fun hasNextQuestion(): Boolean {
        return currentQuestionIndex < questions.size - 1
    }
    
    fun hasPreviousQuestion(): Boolean {
        return currentQuestionIndex > 0
    }
    
    fun nextQuestion(): Question? {
        if (hasNextQuestion()) {
            currentQuestionIndex++
            return getCurrentQuestion()
        }
        return null
    }
    
    fun previousQuestion(): Question? {
        if (hasPreviousQuestion()) {
            currentQuestionIndex--
            return getCurrentQuestion()
        }
        return null
    }
    
    fun submitAnswer(questionId: Int, answer: String, correct: Boolean, timeSpent: Long) {
        userAnswers[questionId] = answer
        questionResults[questionId] = correct
        timeSpentPerQuestion[questionId] = timeSpent
        
        answeredQuestions++
        if (correct) {
            correctAnswers++
        }
    }
    
    fun pauseSession() {
        if (!paused) {
            paused = true
            pausedTime = System.currentTimeMillis()
        }
    }
    
    fun resumeSession() {
        if (paused) {
            paused = false
            totalPausedDuration += System.currentTimeMillis() - pausedTime
            pausedTime = 0L
        }
    }
    
    fun endSession() {
        if (paused) {
            resumeSession()
        }
        endTime = System.currentTimeMillis()
    }
    
    fun isCompleted(): Boolean {
        return currentQuestionIndex >= questions.size || endTime > 0
    }
    
    fun getQuestionsPerMinute(): Double {
        val durationMinutes = getDuration() / (1000.0 * 60.0)
        return if (durationMinutes > 0 && answeredQuestions > 0) {
            answeredQuestions / durationMinutes
        } else {
            0.0
        }
    }
}