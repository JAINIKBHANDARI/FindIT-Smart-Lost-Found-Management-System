package com.example.lostandfound.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.models.Item

class ItemAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    fun updateList(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItemImage: ImageView = itemView.findViewById(R.id.ivItemImage)
        private val tvTypeBadge: TextView = itemView.findViewById(R.id.tvTypeBadge)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        private val tvCategoryLabel: TextView = itemView.findViewById(R.id.tvCategoryLabel)
        private val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(item: Item, onItemClick: (Item) -> Unit) {
            tvItemName.text = item.itemName
            tvCategoryLabel.text = item.category.uppercase()
            tvLocation.text = item.location
            tvDate.text = "${item.date} • ${item.time.ifBlank { "Approx" }}"
            tvStatusBadge.text = item.status

            // Type badge (LOST or FOUND)
            if (item.type.equals("LOST", ignoreCase = true)) {
                tvTypeBadge.text = "LOST"
                tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost)
                tvTypeBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_lost))
            } else {
                tvTypeBadge.text = "FOUND"
                tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_found)
                tvTypeBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_found))
            }

            // Status badge styling
            when (item.status) {
                "Potential Match" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_match)
                    tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_match))
                }
                "Claim Pending" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                    tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_pending))
                }
                "Resolved" -> {
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_found)
                    tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_found))
                }
                else -> {
                    if (item.isSafeHandover()) {
                        tvStatusBadge.text = "🛡️ Safe Handover"
                        tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                        tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
                    } else {
                        tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_lost)
                        tvStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                    }
                }
            }

            // Image display
            com.example.lostandfound.utils.ImageUtils.loadItemImage(ivItemImage, item)

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
