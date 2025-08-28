package com.tkjy.questionsystem.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tkjy.questionsystem.databinding.ItemCategoryChipBinding

class CategoryChipAdapter(
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryChipAdapter.CategoryViewHolder>() {
    
    private var categories: List<String> = emptyList()
    
    fun setCategories(newCategories: List<String>) {
        categories = newCategories
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }
    
    override fun getItemCount(): Int = categories.size
    
    inner class CategoryViewHolder(
        private val binding: ItemCategoryChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(category: String) {
            binding.categoryChip.text = category
            binding.categoryChip.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}