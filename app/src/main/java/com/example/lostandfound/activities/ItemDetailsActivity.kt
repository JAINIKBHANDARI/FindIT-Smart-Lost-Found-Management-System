package com.example.lostandfound.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lostandfound.R
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.ActivityItemDetailsBinding
import com.example.lostandfound.models.Claim
import com.example.lostandfound.models.Item
import com.example.lostandfound.models.User
import com.example.lostandfound.utils.SessionManager

class ItemDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailsBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var currentItem: Item? = null
    private var reporterUser: User? = null
    private var itemId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        itemId = intent.getIntExtra("ITEM_ID", -1)
        if (itemId == -1) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadItemDetails()
    }

    private fun setupViews() {
        binding.btnBackDetails.setOnClickListener { finish() }

        binding.btnShareDetails.setOnClickListener {
            currentItem?.let { item ->
                val shareBody = """
                    📢 Lost and Found Notice:
                    [${item.type}] ${item.itemName}
                    📍 Location: ${item.location}
                    📅 Date: ${item.date}
                    📝 Details: ${item.description}
                    
                    Reported on Lost and Found.
                """.trimIndent()

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Lost and Found: ${item.itemName}")
                    putExtra(Intent.EXTRA_TEXT, shareBody)
                }
                startActivity(Intent.createChooser(shareIntent, "Share item details via"))
            }
        }

        binding.btnReportSuspicious.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Report Suspicious Post")
                .setMessage("Flag this post for review by Campus Security & Administration?")
                .setPositiveButton("Report") { _, _ ->
                    Toast.makeText(this, "Report received. Security desk notified.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadItemDetails() {
        currentItem = dbHelper.getItemById(itemId)
        val item = currentItem
        if (item == null) {
            Toast.makeText(this, "Item could not be loaded", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        reporterUser = dbHelper.getUserById(item.userId)

        // Type badge
        if (item.type.equals("LOST", ignoreCase = true)) {
            binding.tvDetType.text = "LOST"
            binding.tvDetType.setBackgroundResource(R.drawable.bg_badge_lost)
            binding.tvDetType.setTextColor(ContextCompat.getColor(this, R.color.status_lost))
        } else {
            binding.tvDetType.text = "FOUND"
            binding.tvDetType.setBackgroundResource(R.drawable.bg_badge_found)
            binding.tvDetType.setTextColor(ContextCompat.getColor(this, R.color.status_found))
        }

        // Status badge
        binding.tvDetStatus.text = item.status
        when (item.status) {
            "Potential Match" -> {
                binding.tvDetStatus.setBackgroundResource(R.drawable.bg_badge_match)
                binding.tvDetStatus.setTextColor(ContextCompat.getColor(this, R.color.status_match))
            }
            "Claim Pending" -> {
                binding.tvDetStatus.setBackgroundResource(R.drawable.bg_badge_pending)
                binding.tvDetStatus.setTextColor(ContextCompat.getColor(this, R.color.status_pending))
            }
            "Resolved" -> {
                binding.tvDetStatus.setBackgroundResource(R.drawable.bg_badge_found)
                binding.tvDetStatus.setTextColor(ContextCompat.getColor(this, R.color.status_found))
            }
            else -> {
                binding.tvDetStatus.setBackgroundResource(R.drawable.bg_badge_lost)
                binding.tvDetStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            }
        }

        binding.tvDetCategory.text = item.category.uppercase()
        binding.tvDetName.text = item.itemName
        binding.tvDetLocation.text = item.location
        binding.tvDetDate.text = "${item.date}${if (item.time.isNotBlank()) " • ${item.time}" else ""}"
        binding.tvDetDesc.text = if (item.description.isNotBlank()) item.description else "No additional description provided."

        // Image
        com.example.lostandfound.utils.ImageUtils.loadItemImage(binding.ivDetImage, item)

        // Anti-Fraud Ownership Shield
        val isSafe = item.isSafeHandover()
        val currentUserId = sessionManager.getUserId()
        val isOwner = (currentUserId != -1 && currentUserId == item.userId)

        binding.tvShieldBadge.text = if (isSafe) "GCET SAFE ESCROW" else "VERIFIED ID"
        binding.tvHandoverInstructions.text = if (isSafe) {
            "📍 Mandated Safe Handover: GCET Campus Security Desk (Gate #1). Both parties must physically present their GCET Student ID card."
        } else {
            "🛡️ Anti-Fraud Shield: Always verify the claimant's physical GCET College ID Card and cross-check security answers before release."
        }

        val pinCode = item.getSecurityPin()
        var pinRevealed = false
        binding.tvHandoverPin.text = "PIN: #••••"
        binding.btnViewPin.setOnClickListener {
            if (isOwner) {
                pinRevealed = !pinRevealed
                binding.tvHandoverPin.text = if (pinRevealed) "PIN: #$pinCode" else "PIN: #••••"
                binding.btnViewPin.text = if (pinRevealed) "Hide PIN" else "View PIN"
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Handover Security PIN")
                    .setMessage("This item is protected by GCET Anti-Fraud Shield. The 4-digit Security PIN is revealed by the owner/finder upon physical handover once your GCET College ID is verified.")
                    .setPositiveButton("Understood", null)
                    .show()
            }
        }

        binding.tvAntiFraudTipsLink.setOnClickListener {
            showAntiFraudGuideDialog()
        }

        // Verification Question
        if (item.verificationQuestion.isNotBlank()) {
            binding.layoutVerifyQuestionBox.visibility = View.VISIBLE
            binding.tvVerifyQuestionTitle.text = if (item.type.equals("LOST", ignoreCase = true)) {
                "Owner's Proof Challenge:"
            } else {
                "Finder's Verification Question:"
            }
            binding.tvDetVerifyQuestion.text = item.verificationQuestion
        } else {
            binding.layoutVerifyQuestionBox.visibility = View.GONE
        }

        // Reporter
        val reporterName = reporterUser?.name ?: "Campus Member"
        binding.tvDetReporter.text = reporterName
        val initials = reporterName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
        binding.tvReporterAvatarLetter.text = if (initials.isNotEmpty()) initials else "U"

        // Claim Banner Handling
        val claims = dbHelper.getClaimsByItemId(item.id)
        val pendingClaim = claims.firstOrNull { it.status.equals("PENDING", ignoreCase = true) }
        val myClaim = claims.firstOrNull { it.claimantId == currentUserId }

        if (isOwner && claims.isNotEmpty()) {
            binding.cardClaimBanner.visibility = View.VISIBLE
            if (pendingClaim != null) {
                binding.tvClaimBannerTitle.text = "Pending Claim Awaiting Review 📋"
                binding.tvClaimBannerSubtitle.text = "A student has submitted verification to claim this item. Tap to review."
                binding.btnViewClaimDetails.text = "Review Claim Details 📋"
            } else {
                binding.tvClaimBannerTitle.text = "Claim History (${claims.size})"
                binding.tvClaimBannerSubtitle.text = "Latest claim status: ${claims.first().status}"
                binding.btnViewClaimDetails.text = "View Claim Details"
            }
            binding.btnViewClaimDetails.setOnClickListener {
                val intent = Intent(this, ClaimDetailsActivity::class.java).apply {
                    putExtra("ITEM_ID", item.id)
                }
                startActivity(intent)
            }
        } else if (!isOwner && myClaim != null) {
            binding.cardClaimBanner.visibility = View.VISIBLE
            binding.tvClaimBannerTitle.text = "Your Claim Status: ${myClaim.status}"
            binding.tvClaimBannerSubtitle.text = "Tap below to view your claim submission, answers, and safe handover details."
            binding.btnViewClaimDetails.text = "View My Claim Status"
            binding.btnViewClaimDetails.setOnClickListener {
                val intent = Intent(this, ClaimDetailsActivity::class.java).apply {
                    putExtra("CLAIM_ID", myClaim.id)
                    putExtra("ITEM_ID", item.id)
                }
                startActivity(intent)
            }
        } else {
            binding.cardClaimBanner.visibility = View.GONE
        }

        setupActionButtons(item)
    }

    private fun setupActionButtons(item: Item) {
        val currentUserId = sessionManager.getUserId()
        val isOwner = (currentUserId != -1 && currentUserId == item.userId)

        if (isOwner) {
            binding.btnSecondaryAction.text = "Delete Report"
            binding.btnSecondaryAction.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Report")
                    .setMessage("Are you sure you want to delete this report?")
                    .setPositiveButton("Delete") { _, _ ->
                        dbHelper.deleteItem(item.id)
                        Toast.makeText(this, "Report deleted.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            val pendingClaim = dbHelper.getPendingClaimForItem(item.id)
            if (pendingClaim != null) {
                binding.btnPrimaryAction.text = "Review Claim 📋"
                binding.btnPrimaryAction.isEnabled = true
                binding.btnPrimaryAction.setOnClickListener {
                    val intent = Intent(this, ClaimDetailsActivity::class.java).apply {
                        putExtra("CLAIM_ID", pendingClaim.id)
                        putExtra("ITEM_ID", item.id)
                    }
                    startActivity(intent)
                }
            } else if (item.status.equals("Resolved", ignoreCase = true)) {
                binding.btnPrimaryAction.text = "Resolved ✓"
                binding.btnPrimaryAction.isEnabled = false
            } else {
                binding.btnPrimaryAction.text = "Mark Resolved"
                binding.btnPrimaryAction.isEnabled = true
                binding.btnPrimaryAction.setOnClickListener {
                    dbHelper.updateItemStatus(item.id, "Resolved")
                    Toast.makeText(this, "Item marked as resolved! 🎉", Toast.LENGTH_SHORT).show()
                    loadItemDetails()
                }
            }
        } else {
            // Not owner
            binding.btnSecondaryAction.text = "Contact Reporter"
            binding.btnSecondaryAction.setOnClickListener {
                showContactDialog()
            }

            val claims = dbHelper.getClaimsByItemId(item.id)
            val myClaim = claims.firstOrNull { it.claimantId == currentUserId }

            if (myClaim != null) {
                binding.btnPrimaryAction.text = "Claim: ${myClaim.status}"
                binding.btnPrimaryAction.setOnClickListener {
                    val intent = Intent(this, ClaimDetailsActivity::class.java).apply {
                        putExtra("CLAIM_ID", myClaim.id)
                        putExtra("ITEM_ID", item.id)
                    }
                    startActivity(intent)
                }
            } else if (item.type.equals("LOST", ignoreCase = true)) {
                binding.btnPrimaryAction.text = "I Found This"
                binding.btnPrimaryAction.setOnClickListener {
                    showFoundClaimDialog(item)
                }
            } else {
                binding.btnPrimaryAction.text = "Claim Item"
                binding.btnPrimaryAction.setOnClickListener {
                    showFoundClaimDialog(item)
                }
            }
        }
    }

    private fun showContactDialog() {
        val rep = reporterUser
        val phone = rep?.phone?.ifBlank { "+91 98250 12345" } ?: "+91 98250 12345"
        val email = rep?.email?.ifBlank { "jainik.bhandari@gcet.ac.in" } ?: "jainik.bhandari@gcet.ac.in"
        val name = rep?.name ?: "Reporter"

        val view = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 24)
        }

        val tvInfo = TextView(this).apply {
            text = "Connect with $name regarding this item:"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#102A43"))
            setPadding(0, 0, 0, 16)
        }

        val tvP = TextView(this).apply {
            text = "📞 Phone: $phone"
            textSize = 14f
            setPadding(0, 8, 0, 8)
        }

        val tvE = TextView(this).apply {
            text = "✉️ Email: $email"
            textSize = 14f
            setPadding(0, 8, 0, 16)
        }

        view.addView(tvInfo)
        view.addView(tvP)
        view.addView(tvE)

        AlertDialog.Builder(this)
            .setTitle("Contact $name")
            .setView(view)
            .setPositiveButton("Call") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                } catch (e: Exception) {}
            }
            .setNeutralButton("Email") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, "Regarding item: ${currentItem?.itemName}")
                    })
                } catch (e: Exception) {}
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showFoundClaimDialog(item: Item) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_claim, null)

        val layoutVerifyQuestion = dialogView.findViewById<View>(R.id.layoutDialogVerifyQuestion)
        val tvQuestion = dialogView.findViewById<TextView>(R.id.tvDialogQuestion)
        val etAnswer = dialogView.findViewById<EditText>(R.id.etDialogAnswer)
        val etReason = dialogView.findViewById<EditText>(R.id.etDialogReason)
        val etPhone = dialogView.findViewById<EditText>(R.id.etDialogPhone)
        val etStudentId = dialogView.findViewById<EditText>(R.id.etDialogStudentId)
        val cbAgreeCollegeId = dialogView.findViewById<CheckBox>(R.id.cbAgreeCollegeId)

        val currentUserId = sessionManager.getUserId()
        val claimantUser = dbHelper.getUserById(currentUserId)
        if (claimantUser != null) {
            etStudentId?.setText(claimantUser.studentId)
            if (claimantUser.phone.isNotBlank()) etPhone?.setText(claimantUser.phone)
        } else {
            etStudentId?.setText(sessionManager.getStudentId())
        }

        if (item.verificationQuestion.isNotBlank()) {
            layoutVerifyQuestion.visibility = View.VISIBLE
            tvQuestion.text = item.verificationQuestion
        } else {
            layoutVerifyQuestion.visibility = View.GONE
        }

        AlertDialog.Builder(this)
            .setTitle(if (item.type == "LOST") "Report Item Recovery" else "Claim Ownership")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val reason = etReason.text.toString().trim()
                val answer = etAnswer.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val studentId = etStudentId?.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }
                    ?: (claimantUser?.studentId ?: sessionManager.getStudentId())
                val idAgreed = cbAgreeCollegeId?.isChecked ?: true

                if (reason.isEmpty()) {
                    Toast.makeText(this, "Please describe details/proof", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!idAgreed) {
                    Toast.makeText(this, "Please agree to verify your GCET College ID card", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // Fetch claimant details using claimant user ID
                val claimantName = claimantUser?.name?.takeIf { it.isNotBlank() } ?: sessionManager.getUserName()
                val claimantCollegeId = studentId
                val claimantEmail = claimantUser?.email?.takeIf { it.isNotBlank() } ?: sessionManager.getUserEmail()

                val claim = Claim(
                    itemId = item.id,
                    claimantId = currentUserId,
                    claimantName = claimantName,
                    claimantCollegeId = claimantCollegeId,
                    claimantEmail = claimantEmail,
                    reason = if (phone.isNotBlank()) "$reason (Phone: $phone)" else reason,
                    verificationAnswer = answer,
                    status = "PENDING"
                )

                val claimId = dbHelper.addClaim(claim)
                if (claimId > 0) {
                    dbHelper.updateItemStatus(item.id, "Claim Pending")

                    // Detailed notification to finder showing claimant name, college ID, email, verification answer, and status
                    val notifMessage = """
Claimant: $claimantName
College ID: $claimantCollegeId
Email: $claimantEmail
Answer: $answer
Status: PENDING
                    """.trimIndent()

                    dbHelper.addNotification(
                        userId = item.userId,
                        title = "New Claim Request: ${item.itemName} 📋",
                        message = notifMessage,
                        type = "claim",
                        relatedItemId = item.id
                    )
                    Toast.makeText(this, "Claim submitted! The finder has been notified with your claim details. 📬", Toast.LENGTH_LONG).show()
                    loadItemDetails()
                } else {
                    Toast.makeText(this, "Failed to submit claim", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAntiFraudGuideDialog() {
        val guide = """
            🛡️ GCET Anti-Fraud & Wrong Hands Protection Guide:

            1. 🪪 Mandatory College ID Check:
               Always demand and inspect the physical GCET Student ID card before releasing any item. Confirm the student name and department match.

            2. 🔢 Cross-Check the Handover PIN:
               Verify the 4-digit Handover PIN (#••••) generated exclusively for this report before releasing custody.

            3. 🔒 Answer the Secret Challenge:
               Genuine owners will easily confirm lock-screen wallpapers, serial digits, internal contents, or unique scratches.

            4. 🏛️ Hand Over at GCET Security Desk:
               Meet exclusively at GCET Security Desk (Gate #1) or Central Library reception for high-value items (laptops, phones, wallets).

            5. 🚩 Report Impostors:
               If someone attempts fraudulent claiming, notify GCET Campus Security immediately at +91 2692 231651.
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("🛡️ Prevent Wrong Hands (Security Guide)")
            .setMessage(guide)
            .setPositiveButton("I Understand", null)
            .show()
    }
}
