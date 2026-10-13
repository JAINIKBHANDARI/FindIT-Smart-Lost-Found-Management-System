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
import com.google.android.material.button.MaterialButton

class MyReportAdapter(
    private var reports: List<Item>,
    private val onItemClick: (Item) -> Unit,
    private val onResolveClick: (Item) -> Unit,
    private val onDeleteClick: (Item) -> Unit,
    private val onMatchClick: (Item) -> Unit
) : RecyclerView.Adapter<MyReportAdapter.ReportViewHolder>() {

    fun updateList(newReports: List<Item>) {
        reports = newReports
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position], onItemClick, onResolveClick, onDeleteClick, onMatchClick)
    }

    override fun getItemCount(): Int = reports.size

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivReportThumb: ImageView = itemView.findViewById(R.id.ivReportThumb)
        private val tvReportType: TextView = itemView.findViewById(R.id.tvReportType)
        private val tvReportStatus: TextView = itemView.findViewById(R.id.tvReportStatus)
        private val tvReportTitle: TextView = itemView.findViewById(R.id.tvReportTitle)
        private val tvReportMeta: TextView = itemView.findViewById(R.id.tvReportMeta)
        private val btnMatchAction: MaterialButton = itemView.findViewById(R.id.btnMatchAction)
        private val btnResolveAction: MaterialButton = itemView.findViewById(R.id.btnResolveAction)
        private val btnDeleteAction: MaterialButton = itemView.findViewById(R.id.btnDeleteAction)

        fun bind(
            item: Item,
            onItemClick: (Item) -> Unit,
            onResolveClick: (Item) -> Unit,
            onDeleteClick: (Item) -> Unit,
            onMatchClick: (Item) -> Unit
        ) {
            tvReportTitle.text = item.itemName
            tvReportMeta.text = "📍 ${item.location} • ${item.date}"
            tvReportStatus.text = item.status

            if (item.type.equals("LOST", ignoreCase = true)) {
                tvReportType.text = "LOST"
                tvReportType.setBackgroundResource(R.drawable.bg_badge_lost)
                tvReportType.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_lost))
            } else {
                tvReportType.text = "FOUND"
                tvReportType.setBackgroundResource(R.drawable.bg_badge_found)
                tvReportType.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_found))
            }

            // Match Action button visibility
            if (item.status == "Potential Match") {
                btnMatchAction.visibility = View.VISIBLE
                btnMatchAction.setOnClickListener { onMatchClick(item) }
            } else {
                btnMatchAction.visibility = View.GONE
            }

            // Resolve button visibility
            if (item.status == "Resolved") {
                btnResolveAction.visibility = View.GONE
            } else {
                btnResolveAction.visibility = View.VISIBLE
                btnResolveAction.setOnClickListener { onResolveClick(item) }
            }

            btnDeleteAction.setOnClickListener { onDeleteClick(item) }
            itemView.setOnClickListener { onItemClick(item) }

            // Thumbnail
            com.example.lostandfound.utils.ImageUtils.loadItemImage(ivReportThumb, item)
        }
    }
}
