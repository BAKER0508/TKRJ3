package com.tkjy.questionsystem.models

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("question")
    val question: String = "",
    
    @SerializedName("answer")
    val answer: String = "",
    
    @SerializedName("category")
    val category: String = "默认",
    
    @SerializedName("type")
    val type: String = "text", // text, choice, truefalse
    
    @SerializedName("options")
    val options: List<String> = emptyList(),
    
    @SerializedName("correctOption")
    val correctOption: Int = -1,
    
    @SerializedName("difficulty")
    val difficulty: String = "medium", // easy, medium, hard
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("explanation")
    val explanation: String = "",
    
    // Study tracking fields
    @SerializedName("studyCount")
    var studyCount: Int = 0,
    
    @SerializedName("correctCount")
    var correctCount: Int = 0,
    
    @SerializedName("lastStudied")
    var lastStudied: Long = 0L,
    
    @SerializedName("memoryLevel")
    var memoryLevel: Int = 0, // 0-5, used for spaced repetition
    
    @SerializedName("nextReview")
    var nextReview: Long = 0L
) {
    fun getAccuracyRate(): Double {
        return if (studyCount > 0) {
            (correctCount.toDouble() / studyCount.toDouble()) * 100
        } else {
            0.0
        }
    }
    
    fun isNew(): Boolean = studyCount == 0
    
    fun needsReview(): Boolean = System.currentTimeMillis() >= nextReview
    
    fun updateMemoryLevel(correct: Boolean) {
        studyCount++
        if (correct) {
            correctCount++
            memoryLevel = minOf(5, memoryLevel + 1)
        } else {
            memoryLevel = maxOf(0, memoryLevel - 1)
        }
        lastStudied = System.currentTimeMillis()
        
        // Calculate next review time based on memory level (spaced repetition)
        val intervals = longArrayOf(
            1000L * 60 * 10,      // 10 minutes (level 0)
            1000L * 60 * 60,      // 1 hour (level 1)
            1000L * 60 * 60 * 4,  // 4 hours (level 2)
            1000L * 60 * 60 * 24, // 1 day (level 3)
            1000L * 60 * 60 * 24 * 3, // 3 days (level 4)
            1000L * 60 * 60 * 24 * 7  // 7 days (level 5)
        )
        
        nextReview = lastStudied + intervals[memoryLevel]
    }
}