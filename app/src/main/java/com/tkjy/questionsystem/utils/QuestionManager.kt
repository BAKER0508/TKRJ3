package com.tkjy.questionsystem.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tkjy.questionsystem.models.Question

class QuestionManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("questions", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private var questions: MutableList<Question> = mutableListOf()
    
    init {
        loadQuestions()
        if (questions.isEmpty()) {
            // Initialize with sample questions
            initializeSampleQuestions()
        }
    }
    
    private fun loadQuestions() {
        val questionsJson = prefs.getString("questions_data", null)
        if (questionsJson != null) {
            val type = object : TypeToken<List<Question>>() {}.type
            questions = gson.fromJson<List<Question>>(questionsJson, type).toMutableList()
        }
    }
    
    private fun saveQuestions() {
        val questionsJson = gson.toJson(questions)
        prefs.edit().putString("questions_data", questionsJson).apply()
    }
    
    fun saveQuestion(question: Question) {
        val existingIndex = questions.indexOfFirst { it.id == question.id }
        if (existingIndex != -1) {
            questions[existingIndex] = question
        } else {
            questions.add(question)
        }
        saveQuestions()
    }
    
    fun getAllQuestions(): List<Question> = questions.toList()
    
    fun getQuestionsByCategory(category: String): List<Question> {
        return questions.filter { it.category == category || category == "全部分类" }
    }
    
    fun getAllCategories(): List<String> {
        return questions.map { it.category }.distinct().sorted()
    }
    
    fun getQuestionsForMemoryMode(): List<Question> {
        // Return questions with low memory level or need review
        return questions.filter { it.memoryLevel < 3 || it.needsReview() }.shuffled()
    }
    
    fun getQuestionsForReview(): List<Question> {
        return questions.filter { it.needsReview() }.shuffled()
    }
    
    fun getRandomQuestions(count: Int): List<Question> {
        return questions.shuffled().take(count)
    }
    
    fun addQuestions(newQuestions: List<Question>) {
        val maxId = questions.maxOfOrNull { it.id } ?: 0
        newQuestions.forEachIndexed { index, question ->
            questions.add(question.copy(id = maxId + index + 1))
        }
        saveQuestions()
    }
    
    fun deleteQuestion(questionId: Int) {
        questions.removeAll { it.id == questionId }
        saveQuestions()
    }
    
    fun updateQuestion(question: Question) {
        saveQuestion(question)
    }
    
    private fun initializeSampleQuestions() {
        val sampleQuestions = listOf(
            Question(
                id = 1,
                question = "Java中String类型的变量是否可变？",
                answer = "不可变。String对象一旦创建就不能修改，每次对String进行修改操作都会创建新的String对象。",
                category = "Java基础",
                type = "text"
            ),
            Question(
                id = 2,
                question = "什么是Android Activity的生命周期？",
                answer = "Activity生命周期包括：onCreate() -> onStart() -> onResume() -> onPause() -> onStop() -> onDestroy()，以及onRestart()。",
                category = "Android开发",
                type = "text"
            ),
            Question(
                id = 3,
                question = "HTTP协议和HTTPS协议的主要区别是什么？",
                answer = "HTTPS是HTTP的安全版本，使用SSL/TLS加密传输数据，端口443；HTTP是明文传输，端口80。HTTPS提供身份验证、数据完整性和隐私保护。",
                category = "网络协议",
                type = "text"
            ),
            Question(
                id = 4,
                question = "以下哪种数据结构最适合实现栈？",
                answer = "数组",
                options = listOf("链表", "数组", "树", "图"),
                correctOption = 1,
                category = "数据结构",
                type = "choice"
            ),
            Question(
                id = 5,
                question = "SQL中的JOIN操作用于连接两个或多个表。",
                answer = "对",
                category = "数据库",
                type = "truefalse"
            )
        )
        
        questions.addAll(sampleQuestions)
        saveQuestions()
    }
}