package com.example.lostandfound.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lostandfound.R
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivityClaimDetailsBinding
import com.example.lostandfound.models.Claim
import com.example.lostandfound.models.Item
import com.example.lostandfound.models.User
import com.example.lostandfound.utils.SessionManager

class ClaimDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClaimDetailsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var currentClaim: Claim? = null
    private var currentItem: Item? = null
    private var claimantUser: User? = null
    private var pinRevealed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaimDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        setupListeners()
        loadClaimData()
    }

    private fun setupListeners() {
        binding.btnBackClaim.setOnClickListener { finish() }

        binding.btnToggleClaimPin.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            val pin = item.getSecurityPin()
            val currentUserId = sessionManager.getUserId()
            val isFinder = (currentUserId != -1 && currentUserId == item.userId)
            val isClaimant = (currentUserId != -1 && currentClaim != null && currentUserId == currentClaim?.claimantId)
            val isApproved = currentClaim?.status.equals("APPROVED", ignoreCase = true)

            if (isFinder || (isClaimant && isApproved)) {
                pinRevealed = !pinRevealed
                binding.tvClaimHandoverPin.text = if (pinRevealed) "PIN: #$pin" else "PIN: #••••"
                binding.btnToggleClaimPin.text = if (pinRevealed) "Hide PIN" else "View PIN"
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Handover Security PIN")
                    .setMessage("This item is protected by GCET Anti-Fraud Shield. The 4-digit Security PIN is revealed upon claim approval or by the finder during physical handover.")
                    .setPositiveButton("Understood", null)
                    .show()
            }
        }

        binding.btnApproveClaim.setOnClickListener {
            showApproveDialog()
        }

        binding.btnRejectClaim.setOnClickListener {
            showRejectDialog()
        }
    }

    private fun loadClaimData() {
        val claimId = intent.getIntExtra("CLAIM_ID", -1)
        val itemId = intent.getIntExtra("ITEM_ID", -1)

        val claim = if (claimId != -1) {
            dbHelper.getClaimById(claimId)
        } else if (itemId != -1) {
            dbHelper.getPendingClaimForItem(itemId) ?: dbHelper.getClaimsByItemId(itemId).firstOrNull()
        } else {
            null
        }

        if (claim == null) {
            Toast.makeText(this, "Claim not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentClaim = claim
        val item = dbHelper.getItemById(claim.itemId)
        if (item == null) {
            Toast.makeText(this, "Associated item not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentItem = item

        // Use claimant user ID to fetch College ID and user details correctly
        claimantUser = dbHelper.getUserById(claim.claimantId)

        bindDataToViews(claim, item)
    }

    private fun bindDataToViews(claim: Claim, item: Item) {
        // Item Details
        binding.tvClaimItemName.text = item.itemName
        binding.tvClaimItemCategory.text = if (item.category.isNotBlank()) item.category else "Item"
        binding.tvClaimItemLocation.text = "📍 " + if (item.location.isNotBlank()) item.location else "Campus"

        if (item.verificationQuestion.isNotBlank()) {
            binding.layoutClaimVerifyQuestion.visibility = View.VISIBLE
            binding.tvClaimVerifyQuestion.text = item.verificationQuestion
        } else {
            binding.layoutClaimVerifyQuestion.visibility = View.GONE
        }

        // Claimant Information: Use claimant user ID to fetch College ID correctly
        val cUser = claimantUser
        val collegeId = cUser?.studentId?.takeIf { it.isNotBlank() } ?: claim.claimantCollegeId.ifBlank { "N/A" }
        val claimantName = cUser?.name?.takeIf { it.isNotBlank() } ?: claim.claimantName.ifBlank { "Campus Member" }
        val claimantEmail = cUser?.email?.takeIf { it.isNotBlank() } ?: claim.claimantEmail.ifBlank { "N/A" }

        binding.tvClaimantName.text = claimantName
        binding.tvClaimantCollegeId.text = collegeId
        binding.tvClaimantEmail.text = claimantEmail

        // Verification Answer & Proof
        binding.tvClaimVerificationAnswer.text = if (claim.verificationAnswer.isNotBlank()) {
            "\"${claim.verificationAnswer}\""
        } else {
            "No answer provided"
        }
        binding.tvClaimReason.text = if (claim.reason.isNotBlank()) claim.reason else "No additional notes provided"
        binding.tvClaimCreatedAt.text = "Submitted: " + (if (claim.createdAt.isNotBlank()) claim.createdAt else "Recently")

        // Handover PIN & Safe Campus Location
        binding.tvClaimHandoverPin.text = "PIN: #••••"
        binding.btnToggleClaimPin.text = "View PIN"
        pinRevealed = false

        val safeLoc = if (item.location.isNotBlank()) item.location else "GCET Central Library / Security Desk Gate #1"
        binding.tvSafeHandoverLocation.text = "Safe Campus Meeting Location: $safeLoc"

        // Status Styling & Action Controls
        val currentUserId = sessionManager.getUserId()
        val isFinder = (currentUserId != -1 && currentUserId == item.userId)

        when (claim.status.uppercase()) {
            "APPROVED" -> {
                binding.tvClaimStatusBadge.text = "APPROVED ✓"
                binding.tvClaimStatusBadge.setBackgroundResource(R.drawable.bg_badge_found)
                binding.tvClaimStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_found))

                binding.cardStatusBanner.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_found_light))
                binding.cardStatusBanner.strokeColor = ContextCompat.getColor(this, R.color.status_found)
                binding.tvStatusBannerIcon.text = "✅"
                binding.tvStatusBannerTitle.text = "Claim Approved"
                binding.tvStatusBannerMessage.text = "The claimant has been notified to meet at a safe campus location ($safeLoc) with their GCET College ID ($collegeId) and Handover PIN: #${item.getSecurityPin()}."

                binding.layoutClaimActions.visibility = View.GONE
            }
            "REJECTED" -> {
                binding.tvClaimStatusBadge.text = "REJECTED ✕"
                binding.tvClaimStatusBadge.setBackgroundResource(R.drawable.bg_badge_lost)
                binding.tvClaimStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.status_lost))

                binding.cardStatusBanner.setCardBackgroundColor(ContextCompat.getColor(this, R.color.status_lost_light))
                binding.cardStatusBanner.strokeColor = ContextCompat.getColor(this, R.color.status_lost)
                binding.tvStatusBannerIcon.text = "❌"
                binding.tvStatusBannerTitle.text = "Claim Rejected"
                binding.tvStatusBannerMessage.text = "This claim was not approved. History is safely preserved in records."

                binding.layoutClaimActions.visibility = View.GONE
            }
            else -> { // PENDING
                binding.tvClaimStatusBadge.text = "PENDING"
                binding.tvClaimStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                binding.tvClaimStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.primary))

                binding.cardStatusBanner.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_light))
                binding.cardStatusBanner.strokeColor = ContextCompat.getColor(this, R.color.primary)
                binding.tvStatusBannerIcon.text = "📋"

                if (isFinder) {
                    binding.tvStatusBannerTitle.text = "Review Claimant Submission"
                    binding.tvStatusBannerMessage.text = "Check the claimant's verification answer against your item's question and verify their GCET College ID."
                    binding.layoutClaimActions.visibility = View.VISIBLE
                } else {
                    binding.tvStatusBannerTitle.text = "Claim Under Review"
                    binding.tvStatusBannerMessage.text = "Your claim has been submitted to the finder. You will receive a notification once reviewed."
                    binding.layoutClaimActions.visibility = View.GONE
                }
            }
        }
    }

    private fun showApproveDialog() {
        val claim = currentClaim ?: return
        val item = currentItem ?: return
        val cUser = claimantUser
        val collegeId = cUser?.studentId ?: claim.claimantCollegeId
        val pin = item.getSecurityPin()

        AlertDialog.Builder(this)
            .setTitle("Approve Claim? ✓")
            .setMessage("Are you sure you want to approve this claim? The claimant will be notified to meet at a safe campus location with their College ID ($collegeId) and Handover PIN: #$pin.")
            .setPositiveButton("Approve") { _, _ ->
                executeApproveClaim(claim, item, collegeId, pin)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeApproveClaim(claim: Claim, item: Item, collegeId: String, pin: String) {
        dbHelper.updateClaimStatus(claim.id, "APPROVED")
        dbHelper.updateItemStatus(item.id, "Resolved")

        val safeLoc = if (item.location.isNotBlank()) item.location else "GCET Campus Security Desk Gate #1"
        val approveNotifMessage = "Your claim for '${item.itemName}' was approved! Please meet at a safe campus location ($safeLoc) with your College ID ($collegeId) and Handover PIN: #$pin."

        dbHelper.addNotification(
            userId = claim.claimantId,
            title = "Claim Approved! 🎉 Meet for Handover",
            message = approveNotifMessage,
            type = "claim_approved",
            relatedItemId = item.id
        )

        Toast.makeText(this, "Claim approved! Claimant notified to meet at safe campus location with PIN #$pin. 🎉", Toast.LENGTH_LONG).show()
        loadClaimData()
    }

    private fun showRejectDialog() {
        val claim = currentClaim ?: return
        val item = currentItem ?: return

        AlertDialog.Builder(this)
            .setTitle("Reject Claim? ✕")
            .setMessage("Are you sure you want to reject this claim? The claim history will be kept, and the claimant will be notified.")
            .setPositiveButton("Reject") { _, _ ->
                executeRejectClaim(claim, item)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun executeRejectClaim(claim: Claim, item: Item) {
        // On rejection, notify without deleting history
        dbHelper.updateClaimStatus(claim.id, "REJECTED")

        // Revert item status to Active if there are no other pending claims
        val remainingPending = dbHelper.getPendingClaimForItem(item.id)
        if (remainingPending == null) {
            dbHelper.updateItemStatus(item.id, "Active")
        }

        val rejectNotifMessage = "Your claim for '${item.itemName}' could not be verified by the finder. History has been preserved. If you have questions, please visit GCET Campus Security Desk."

        dbHelper.addNotification(
            userId = claim.claimantId,
            title = "Claim Rejected ❌",
            message = rejectNotifMessage,
            type = "claim_rejected",
            relatedItemId = item.id
        )

        Toast.makeText(this, "Claim rejected. Claimant notified and history preserved.", Toast.LENGTH_LONG).show()
        loadClaimData()
    }
}
