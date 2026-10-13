package com.example.lostandfound.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.example.lostandfound.R
import com.example.lostandfound.models.Item
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun getDefaultCategoryDrawable(category: String, itemName: String = ""): Int {
        val lowerName = itemName.lowercase()
        return when {
            // Electronics & Gadgets
            lowerName.contains("airpod") || lowerName.contains("earbud") || lowerName.contains("headphone") ||
            lowerName.contains("phone") || lowerName.contains("iphone") || lowerName.contains("samsung") ||
            lowerName.contains("mobile") || lowerName.contains("laptop") || lowerName.contains("macbook") ||
            lowerName.contains("charger") || lowerName.contains("cable") || lowerName.contains("watch") ||
            lowerName.contains("ipad") || lowerName.contains("tablet") || lowerName.contains("bluetooth") -> R.drawable.img_airpods

            // Books, Stationery & Calculators
            lowerName.contains("calculator") || lowerName.contains("casio") || lowerName.contains("book") ||
            lowerName.contains("notebook") || lowerName.contains("pen") || lowerName.contains("pencil") ||
            lowerName.contains("diary") || lowerName.contains("register") -> R.drawable.img_calculator

            // Keys & Locks
            lowerName.contains("key") || lowerName.contains("lock") || lowerName.contains("keychain") ||
            lowerName.contains("lanyard") -> R.drawable.img_keys

            // ID Cards, Badges & Passes
            lowerName.contains("id") || lowerName.contains("card") || lowerName.contains("pass") ||
            lowerName.contains("badge") || lowerName.contains("license") || lowerName.contains("licence") ||
            lowerName.contains("hall ticket") || lowerName.contains("admit card") -> R.drawable.img_id_card

            // Wallets & Documents
            lowerName.contains("wallet") || lowerName.contains("purse") || lowerName.contains("money") ||
            lowerName.contains("cash") || lowerName.contains("document") || lowerName.contains("file") ||
            lowerName.contains("folder") || lowerName.contains("certificate") -> R.drawable.img_wallet

            // Bags, Backpacks, Clothing & Accessories
            lowerName.contains("bag") || lowerName.contains("backpack") || lowerName.contains("pouch") ||
            lowerName.contains("tote") || lowerName.contains("bottle") || lowerName.contains("flask") ||
            lowerName.contains("jacket") || lowerName.contains("hoodie") || lowerName.contains("umbrella") ||
            lowerName.contains("cap") || lowerName.contains("glasses") || lowerName.contains("spectacles") -> R.drawable.img_backpack

            // Category fallbacks
            category.equals("Electronics", ignoreCase = true) -> R.drawable.img_airpods
            category.equals("Keys", ignoreCase = true) -> R.drawable.img_keys
            category.equals("ID Cards", ignoreCase = true) -> R.drawable.img_id_card
            category.equals("Bags", ignoreCase = true) -> R.drawable.img_backpack
            category.equals("Documents", ignoreCase = true) -> R.drawable.img_wallet
            category.equals("Books", ignoreCase = true) -> R.drawable.img_calculator
            category.equals("Clothing", ignoreCase = true) -> R.drawable.img_backpack
            else -> R.drawable.img_backpack
        }
    }

    fun getDefaultCategoryUri(packageName: String, category: String, itemName: String = ""): String {
        val resId = getDefaultCategoryDrawable(category, itemName)
        val resName = when (resId) {
            R.drawable.img_airpods -> "img_airpods"
            R.drawable.img_calculator -> "img_calculator"
            R.drawable.img_keys -> "img_keys"
            R.drawable.img_id_card -> "img_id_card"
            R.drawable.img_backpack -> "img_backpack"
            R.drawable.img_wallet -> "img_wallet"
            else -> "img_airpods"
        }
        return "android.resource://$packageName/drawable/$resName"
    }

    fun loadItemImage(imageView: ImageView, item: Item) {
        val defaultRes = getDefaultCategoryDrawable(item.category, item.itemName)
        val uriStr = item.imageUri.trim()

        if (uriStr.isBlank()) {
            imageView.setImageResource(defaultRes)
            return
        }

        try {
            if (uriStr.startsWith("android.resource://")) {
                val uri = Uri.parse(uriStr)
                val lastSegment = uri.lastPathSegment ?: ""
                val resId = when (lastSegment) {
                    "img_airpods" -> R.drawable.img_airpods
                    "img_calculator" -> R.drawable.img_calculator
                    "img_keys" -> R.drawable.img_keys
                    "img_id_card" -> R.drawable.img_id_card
                    "img_backpack" -> R.drawable.img_backpack
                    "img_wallet" -> R.drawable.img_wallet
                    else -> imageView.context.resources.getIdentifier(lastSegment, "drawable", imageView.context.packageName)
                }
                if (resId != 0) {
                    imageView.setImageResource(resId)
                } else {
                    imageView.setImageURI(uri)
                }
            } else {
                imageView.setImageURI(Uri.parse(uriStr))
            }
        } catch (e: Exception) {
            imageView.setImageResource(defaultRes)
        }

        if (imageView.drawable == null) {
            imageView.setImageResource(defaultRes)
        }
    }

    fun savePickedImage(context: Context, sourceUri: Uri): String {
        return try {
            val dir = File(context.filesDir, "item_images").apply { mkdirs() }
            val file = File(dir, "item_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            sourceUri.toString()
        }
    }
}
