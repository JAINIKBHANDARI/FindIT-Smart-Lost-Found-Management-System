package com.example.lostandfound.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lostandfound.R
import com.example.lostandfound.activities.MainActivity
import com.example.lostandfound.database.DatabaseHelper
import com.example.lostandfound.databinding.FragmentReportItemBinding
import com.example.lostandfound.models.Item
import com.example.lostandfound.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReportItemFragment : Fragment() {

    private var _binding: FragmentReportItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    private var currentType: String = "LOST" // "LOST" or "FOUND"
    private var selectedImageUri: String = ""
    private val calendar = Calendar.getInstance()

    private val categories = listOf(
        "Electronics", "Keys", "ID Cards", "Bags", "Documents", "Clothing", "Books", "Other"
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentUri = com.example.lostandfound.utils.ImageUtils.savePickedImage(requireContext(), uri)
            selectedImageUri = permanentUri
            binding.ivPreviewSelected.setImageURI(Uri.parse(permanentUri))
            binding.layoutPickPrompt.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupSpinners()
        setupDateTimePickers()
        setupTypeToggle()
        setupPhotoPicker()
        setupSubmitButton()
    }

    fun setType(type: String) {
        currentType = type
        if (_binding != null) {
            updateTypeUi()
        }
    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner_selected, categories)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupDateTimePickers() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(calendar.getTime()))
        binding.etTime.setText(timeFormat.format(calendar.getTime()))

        binding.etDate.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    binding.etDate.setText(dateFormat.format(calendar.getTime()))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        binding.etTime.setOnClickListener {
            val timePicker = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    binding.etTime.setText(timeFormat.format(calendar.getTime()))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePicker.show()
        }
    }

    private fun setupTypeToggle() {
        binding.cardChoiceLost.setOnClickListener {
            currentType = "LOST"
            updateTypeUi()
        }

        binding.cardChoiceFound.setOnClickListener {
            currentType = "FOUND"
            updateTypeUi()
        }

        updateTypeUi()
    }

    private fun updateTypeUi() {
        val context = requireContext()
        if (currentType == "LOST") {
            binding.cardChoiceLost.strokeColor = ContextCompat.getColor(context, R.color.status_lost)
            binding.cardChoiceLost.strokeWidth = 4
            binding.cardChoiceLost.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_lost_light))

            binding.cardChoiceFound.strokeColor = ContextCompat.getColor(context, R.color.card_stroke)
            binding.cardChoiceFound.strokeWidth = 2
            binding.cardChoiceFound.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_card))

            binding.tilVerifyQuestion.hint = "Secret Proof Challenge (e.g. Lockscreen wallpaper)"
            binding.tilVerifyQuestion.helperText = "The finder must answer this challenge to prove they hold your item."
        } else {
            binding.cardChoiceFound.strokeColor = ContextCompat.getColor(context, R.color.status_found)
            binding.cardChoiceFound.strokeWidth = 4
            binding.cardChoiceFound.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_found_light))

            binding.cardChoiceLost.strokeColor = ContextCompat.getColor(context, R.color.card_stroke)
            binding.cardChoiceLost.strokeWidth = 2
            binding.cardChoiceLost.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_card))

            binding.tilVerifyQuestion.hint = "Secret Verification Question (e.g. Sticker or case detail)"
            binding.tilVerifyQuestion.helperText = "Claimants must answer this to prevent items going into wrong hands."
        }
    }

    private fun setupPhotoPicker() {
        binding.cardPickPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmitReport.setOnClickListener {
            val itemName = binding.etItemName.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem?.toString() ?: "Other"
            val location = binding.etLocation.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
            val time = binding.etTime.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val verifyQuestion = binding.etVerifyQuestion.text.toString().trim()
            val isSafeHandover = binding.switchSafeHandover.isChecked

            if (itemName.isEmpty()) {
                binding.etItemName.error = "Please enter item name"
                binding.etItemName.requestFocus()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                binding.etLocation.error = "Please enter location"
                binding.etLocation.requestFocus()
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            val newItem = Item(
                userId = userId,
                type = currentType,
                itemName = itemName,
                category = category,
                location = location,
                date = date,
                time = time,
                description = description,
                additionalInfo = if (isSafeHandover) "SAFE_HANDOVER" else "",
                verificationQuestion = verifyQuestion,
                imageUri = if (selectedImageUri.isNotBlank()) {
                    selectedImageUri
                } else {
                    com.example.lostandfound.utils.ImageUtils.getDefaultCategoryUri(requireContext().packageName, category, itemName)
                },
                status = "Active"
            )

            val insertedId = dbHelper.addItem(newItem)
            if (insertedId > 0) {
                newItem.id = insertedId.toInt()

                // Check smart matches
                val matches = dbHelper.findPotentialMatches(newItem)
                if (matches.isNotEmpty()) {
                    val topMatch = matches.first()
                    dbHelper.updateItemStatus(newItem.id, "Potential Match")
                    dbHelper.updateItemStatus(topMatch.first.id, "Potential Match")

                    dbHelper.addNotification(
                        userId = userId,
                        title = "Possible Match Found! 🔎",
                        message = "A possible match was found for your ${newItem.itemName} at ${topMatch.first.location} (${topMatch.second.score}% match)!",
                        type = "match",
                        relatedItemId = topMatch.first.id
                    )
                } else {
                    dbHelper.addNotification(
                        userId = userId,
                        title = "Report Submitted! 📋",
                        message = "Your report for '${newItem.itemName}' is active and visible on Lost and Found.",
                        type = "claim",
                        relatedItemId = newItem.id
                    )
                }

                Toast.makeText(requireContext(), "Item reported successfully! 🚀", Toast.LENGTH_SHORT).show()
                resetForm()

                // Navigate to My Reports tab
                (activity as? MainActivity)?.navigateToMyReports()
            } else {
                Toast.makeText(requireContext(), "Failed to save report. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetForm() {
        binding.etItemName.text = null
        binding.etLocation.text = null
        binding.etDescription.text = null
        binding.etVerifyQuestion.text = null
        selectedImageUri = ""
        binding.ivPreviewSelected.setImageDrawable(null)
        binding.layoutPickPrompt.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
