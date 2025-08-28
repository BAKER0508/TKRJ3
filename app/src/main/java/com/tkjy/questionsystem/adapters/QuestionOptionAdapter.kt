package com.tkjy.questionsystem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tkjy.questionsystem.R
import com.tkjy.questionsystem.databinding.ItemQuestionOptionBinding

class QuestionOptionAdapter(
    private val onOptionClick: (String, Int) -> Unit
) : RecyclerView.Adapter<QuestionOptionAdapter.OptionViewHolder>() {
    
    private var options: List<String> = emptyList()
    private var selectedOption: Int = -1
    private var correctOption: Int = -1
    private var showCorrectAnswer: Boolean = false
    
    fun setOptions(newOptions: List<String>, correct: Int = -1) {
        options = newOptions
        correctOption = correct
        showCorrectAnswer = correct != -1
        selectedOption = -1
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemQuestionOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OptionViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position], position)
    }
    
    override fun getItemCount(): Int = options.size
    
    inner class OptionViewHolder(
        private val binding: ItemQuestionOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(option: String, position: Int) {
            binding.optionText.text = "${('A' + position)}. $option"
            
            // Set background and text color based on state
            when {
                showCorrectAnswer && position == correctOption -> {
                    // Correct answer
                    binding.optionButton.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.success_color)
                    )
                }
                showCorrectAnswer && position == selectedOption && position != correctOption -> {
                    // Wrong answer that was selected
                    binding.optionButton.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.error_color)
                    )
                }
                position == selectedOption -> {
                    // Selected but not yet confirmed
                    binding.optionButton.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.primary_blue_light)
                    )
                }
                else -> {
                    // Default state
                    binding.optionButton.setBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.surface_color)
                    )
                }
            }
            
            binding.optionButton.setOnClickListener {
                if (!showCorrectAnswer) {
                    selectedOption = position
                    notifyDataSetChanged()
                    onOptionClick(option, position)
                }
            }
        }
    }
}