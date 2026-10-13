package com.example.lostandfound.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.models.NotificationItem

class NotificationAdapter(
    private var notifs: List<NotificationItem>,
    private val onNotifClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotifViewHolder>() {

    fun updateList(newList: List<NotificationItem>) {
        notifs = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotifViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        holder.bind(notifs[position], onNotifClick)
    }

    override fun getItemCount(): Int = notifs.size

    class NotifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconContainer)
        private val tvNotifIcon: TextView = itemView.findViewById(R.id.tvNotifIcon)
        private val tvNotifTitle: TextView = itemView.findViewById(R.id.tvNotifTitle)
        private val tvNotifMessage: TextView = itemView.findViewById(R.id.tvNotifMessage)
        private val tvNotifTime: TextView = itemView.findViewById(R.id.tvNotifTime)
        private val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)

        fun bind(notif: NotificationItem, onClick: (NotificationItem) -> Unit) {
            tvNotifTitle.text = notif.title
            tvNotifMessage.text = notif.message
            tvNotifTime.text = notif.timeAgo

            viewUnreadDot.visibility = if (notif.isRead) View.GONE else View.VISIBLE

            when (notif.type) {
                "match" -> {
                    tvNotifIcon.text = "🔎"
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_match)
                }
                "claim", "claim_request" -> {
                    tvNotifIcon.text = "📋"
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_pending)
                }
                "claim_approved" -> {
                    tvNotifIcon.text = "✅"
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_found)
                }
                "claim_rejected" -> {
                    tvNotifIcon.text = "❌"
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_lost)
                }
                "returned" -> {
                    tvNotifIcon.text = "🤝"
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_found)
                }
                else -> {
                    tvNotifIcon.text = "🛡️"
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_lost)
                }
            }

            itemView.setOnClickListener { onClick(notif) }
        }
    }
}
