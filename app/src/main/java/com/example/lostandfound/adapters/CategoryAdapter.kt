package com.example.lostandfound.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.models.Category
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = -1

    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val cat = categories[position]
        holder.bind(cat, position == selectedPosition) {
            val previousSelected = selectedPosition
            selectedPosition = if (selectedPosition == position) -1 else position
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onCategoryClick(cat)
        }
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCategory: MaterialCardView = itemView.findViewById(R.id.cardCategory)
        private val tvCategoryEmoji: TextView = itemView.findViewById(R.id.tvCategoryEmoji)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCategoryCount: TextView = itemView.findViewById(R.id.tvCategoryCount)

        fun bind(category: Category, isSelected: Boolean, onClick: () -> Unit) {
            tvCategoryEmoji.text = category.iconEmoji
            tvCategoryName.text = category.name
            tvCategoryCount.text = "${category.count} items"

            if (isSelected) {
                cardCategory.strokeColor = ContextCompat.getColor(itemView.context, R.color.primary)
                cardCategory.strokeWidth = 4
                cardCategory.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.primary_light))
            } else {
                cardCategory.strokeColor = ContextCompat.getColor(itemView.context, R.color.card_stroke)
                cardCategory.strokeWidth = 2
                cardCategory.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.surface_card))
            }

            itemView.setOnClickListener { onClick() }
        }
    }
}
