package com.example.arcadebits.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arcadebits.R
import com.example.arcadebits.model.CategoryModel

class CategoryAdapter(
    private val categories: List<CategoryModel>,
    private val onClick: (CategoryModel) -> Unit,
    initialCategory: String? = null
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    init {
        initialCategory?.let { initial ->
            val index = categories.indexOfFirst {
                it.title.equals(initial, ignoreCase = true)
            }
            if (index != -1) selectedPosition = index
        }
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.categoryItemContainer)
        val icon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val title: TextView = itemView.findViewById(R.id.categoryTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_view, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.icon.setImageResource(category.iconResId)
        holder.title.text = category.title

        val isSelected = (selectedPosition >= 0) && (position == selectedPosition)
        holder.container.setBackgroundResource(
            if (isSelected) R.drawable.bg_selected_category else R.drawable.bg_default_category
        )

        holder.itemView.setOnClickListener {
            val newPosition = holder.adapterPosition
            if (newPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)

            onClick(categories[newPosition])
        }
    }

    override fun getItemCount() = categories.size




    fun updateSelectedCategory(newCategory: String) {
        val index = categories.indexOfFirst {
            it.title.equals(newCategory, ignoreCase = true)
        }
        if (index != -1 && index != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = index
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }




    fun clearSelection() {
        val old = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
    }



}
