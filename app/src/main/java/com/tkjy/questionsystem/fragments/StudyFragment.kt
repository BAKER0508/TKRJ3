package com.tkjy.questionsystem.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tkjy.questionsystem.R
import com.tkjy.questionsystem.adapters.CategoryChipAdapter
import com.tkjy.questionsystem.adapters.QuestionOptionAdapter
import com.tkjy.questionsystem.databinding.FragmentStudyBinding
import com.tkjy.questionsystem.models.Question
import com.tkjy.questionsystem.models.StudySession
import com.tkjy.questionsystem.utils.QuestionManager
import com.tkjy.questionsystem.utils.StatisticsManager
import java.text.SimpleDateFormat
import java.util.*

class StudyFragment : Fragment() {
    
    private var _binding: FragmentStudyBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var questionManager: QuestionManager
    private lateinit var statisticsManager: StatisticsManager
    
    private var currentStudySession: StudySession? = null
    private var questionStartTime: Long = 0L
    
    private lateinit var categoryAdapter: CategoryChipAdapter
    private lateinit var optionAdapter: QuestionOptionAdapter
    
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        questionManager = QuestionManager(requireContext())
        statisticsManager = StatisticsManager(requireContext())
        
        setupViews()
        setupListeners()
        updateDashboard()
    }
    
    private fun setupViews() {
        // Setup category recycler view
        categoryAdapter = CategoryChipAdapter { category ->
            showCategorySelection("practice", category)
        }
        binding.categoryRecyclerView.layoutManager = 
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.adapter = categoryAdapter
        
        // Setup options recycler view
        optionAdapter = QuestionOptionAdapter { option, index ->
            handleOptionSelected(option, index)
        }
        binding.optionsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.optionsRecyclerView.adapter = optionAdapter
        
        // Show dashboard initially
        showDashboard()
    }
    
    private fun setupListeners() {
        // Study mode buttons
        binding.btnMemoryMode.setOnClickListener { startStudy("memory") }
        binding.btnPracticeMode.setOnClickListener { startStudy("practice") }
        binding.btnTestMode.setOnClickListener { startStudy("test") }
        binding.btnReviewMode.setOnClickListener { startStudy("review") }
        
        // Study controls
        binding.btnShowAnswer.setOnClickListener { showAnswer() }
        binding.btnNextQuestion.setOnClickListener { nextQuestion() }
        binding.btnPrevQuestion.setOnClickListener { previousQuestion() }
        binding.btnEndStudy.setOnClickListener { endStudySession() }
        
        // Memory mode controls
        binding.btnRemember.setOnClickListener { handleMemoryResponse(true) }
        binding.btnNotRemember.setOnClickListener { handleMemoryResponse(false) }
        
        // Verification controls
        binding.btnVerifyCorrect.setOnClickListener { handleVerification(true) }
        binding.btnVerifyIncorrect.setOnClickListener { handleVerification(false) }
    }
    
    private fun startStudy(mode: String) {
        val questions = when (mode) {
            "memory" -> questionManager.getQuestionsForMemoryMode()
            "practice" -> questionManager.getAllQuestions()
            "test" -> questionManager.getRandomQuestions(50)
            "review" -> questionManager.getQuestionsForReview()
            else -> questionManager.getAllQuestions()
        }
        
        if (questions.isEmpty()) {
            // Show message that no questions are available
            return
        }
        
        // Create study session
        currentStudySession = StudySession(
            id = UUID.randomUUID().toString(),
            mode = mode,
            category = "全部分类",
            questions = questions.toMutableList(),
            totalQuestions = questions.size
        )
        
        showStudyInterface()
        displayCurrentQuestion()
    }
    
    private fun showCategorySelection(mode: String, category: String) {
        val questions = questionManager.getQuestionsByCategory(category)
        if (questions.isEmpty()) return
        
        currentStudySession = StudySession(
            id = UUID.randomUUID().toString(),
            mode = mode,
            category = category,
            questions = questions.toMutableList(),
            totalQuestions = questions.size
        )
        
        showStudyInterface()
        displayCurrentQuestion()
    }
    
    private fun showDashboard() {
        binding.dashboardLayout.visibility = View.VISIBLE
        binding.studyInterface.visibility = View.GONE
    }
    
    private fun showStudyInterface() {
        binding.dashboardLayout.visibility = View.GONE
        binding.studyInterface.visibility = View.VISIBLE
        
        currentStudySession?.let { session ->
            binding.studyModeTitle.text = when (session.mode) {
                "memory" -> getString(R.string.memory_mode)
                "practice" -> getString(R.string.practice_mode)
                "test" -> getString(R.string.test_mode)
                "review" -> getString(R.string.review_mode)
                else -> getString(R.string.practice_mode)
            }
        }
    }
    
    private fun displayCurrentQuestion() {
        val session = currentStudySession ?: return
        val question = session.getCurrentQuestion() ?: return
        
        questionStartTime = System.currentTimeMillis()
        
        // Update progress
        binding.questionProgress.text = "${session.currentQuestionIndex + 1}/${session.totalQuestions}"
        binding.studyProgress.progress = session.getProgress().toInt()
        
        // Display question
        binding.questionText.text = question.question
        
        // Hide all control panels initially
        hideAllControls()
        
        // Show appropriate interface based on study mode
        when (session.mode) {
            "memory" -> displayMemoryMode(question)
            "practice", "test" -> displayPracticeMode(question)
            "review" -> displayReviewMode(question)
        }
    }
    
    private fun displayMemoryMode(question: Question) {
        binding.memoryControls.visibility = View.VISIBLE
        binding.btnShowAnswer.visibility = View.VISIBLE
    }
    
    private fun displayPracticeMode(question: Question) {
        when (question.type) {
            "choice" -> {
                binding.optionsRecyclerView.visibility = View.VISIBLE
                optionAdapter.setOptions(question.options, -1)
            }
            "truefalse" -> {
                binding.optionsRecyclerView.visibility = View.VISIBLE
                optionAdapter.setOptions(listOf("对", "错"), -1)
            }
            else -> {
                binding.btnShowAnswer.visibility = View.VISIBLE
            }
        }
    }
    
    private fun displayReviewMode(question: Question) {
        // Show answer immediately for review
        showAnswer()
        binding.verificationControls.visibility = View.VISIBLE
    }
    
    private fun showAnswer() {
        val session = currentStudySession ?: return
        val question = session.getCurrentQuestion() ?: return
        
        binding.answerCard.visibility = View.VISIBLE
        binding.answerText.text = question.answer
        
        if (session.mode == "memory") {
            binding.memoryControls.visibility = View.GONE
            binding.verificationControls.visibility = View.VISIBLE
        }
        
        binding.btnShowAnswer.visibility = View.GONE
    }
    
    private fun handleMemoryResponse(remember: Boolean) {
        val session = currentStudySession ?: return
        val question = session.getCurrentQuestion() ?: return
        
        if (!remember) {
            // Show answer for verification
            showAnswer()
        } else {
            // Mark as correct and move to next question
            val timeSpent = System.currentTimeMillis() - questionStartTime
            session.submitAnswer(question.id, "remembered", true, timeSpent)
            question.updateMemoryLevel(true)
            questionManager.saveQuestion(question)
            
            nextQuestion()
        }
    }
    
    private fun handleVerification(correct: Boolean) {
        val session = currentStudySession ?: return
        val question = session.getCurrentQuestion() ?: return
        
        val timeSpent = System.currentTimeMillis() - questionStartTime
        session.submitAnswer(question.id, if (correct) "correct" else "incorrect", correct, timeSpent)
        question.updateMemoryLevel(correct)
        questionManager.saveQuestion(question)
        
        // Auto advance after short delay
        handler.postDelayed({
            nextQuestion()
        }, 1500)
    }
    
    private fun handleOptionSelected(option: String, index: Int) {
        val session = currentStudySession ?: return
        val question = session.getCurrentQuestion() ?: return
        
        val correct = when (question.type) {
            "choice" -> index == question.correctOption
            "truefalse" -> (option == "对" && question.answer.contains("对|正确|是|True".toRegex(RegexOption.IGNORE_CASE))) ||
                         (option == "错" && question.answer.contains("错|错误|否|False".toRegex(RegexOption.IGNORE_CASE)))
            else -> false
        }
        
        val timeSpent = System.currentTimeMillis() - questionStartTime
        session.submitAnswer(question.id, option, correct, timeSpent)
        question.updateMemoryLevel(correct)
        questionManager.saveQuestion(question)
        
        // Show correct answer
        optionAdapter.setOptions(question.options, question.correctOption)
        
        // Auto advance after delay
        handler.postDelayed({
            nextQuestion()
        }, 2000)
    }
    
    private fun nextQuestion() {
        val session = currentStudySession ?: return
        
        if (session.hasNextQuestion()) {
            session.nextQuestion()
            displayCurrentQuestion()
        } else {
            endStudySession()
        }
    }
    
    private fun previousQuestion() {
        val session = currentStudySession ?: return
        
        if (session.hasPreviousQuestion()) {
            session.previousQuestion()
            displayCurrentQuestion()
        }
    }
    
    private fun endStudySession() {
        currentStudySession?.let { session ->
            session.endSession()
            statisticsManager.saveStudySession(session)
        }
        
        currentStudySession = null
        showDashboard()
        updateDashboard()
    }
    
    private fun hideAllControls() {
        binding.answerCard.visibility = View.GONE
        binding.optionsRecyclerView.visibility = View.GONE
        binding.memoryControls.visibility = View.GONE
        binding.verificationControls.visibility = View.GONE
        binding.btnShowAnswer.visibility = View.GONE
    }
    
    private fun updateDashboard() {
        val todayStats = statisticsManager.getTodayStatistics()
        
        binding.todayQuestions.text = todayStats.totalQuestions.toString()
        binding.todayAccuracy.text = String.format("%.1f%%", todayStats.accuracyRate)
        binding.todayTime.text = formatDuration(todayStats.totalStudyTime)
        
        // Update categories
        val categories = questionManager.getAllCategories()
        categoryAdapter.setCategories(categories)
    }
    
    private fun formatDuration(milliseconds: Long): String {
        val minutes = milliseconds / (1000 * 60)
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        
        return when {
            hours > 0 -> "${hours}h ${remainingMinutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}