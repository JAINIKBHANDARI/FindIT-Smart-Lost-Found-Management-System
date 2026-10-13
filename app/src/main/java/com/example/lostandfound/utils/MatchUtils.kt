package com.example.lostandfound.utils

import com.example.lostandfound.models.Item

object MatchUtils {

    data class MatchResult(
        val score: Int,
        val label: String,
        val matchedCategory: Boolean,
        val matchedLocation: Boolean,
        val matchedKeyword: Boolean
    )

    fun calculateMatch(lostItem: Item, foundItem: Item): MatchResult {
        var score = 0
        var matchedCategory = false
        var matchedLocation = false
        var matchedKeyword = false

        // 1. Category match (30%)
        if (lostItem.category.isNotBlank() && lostItem.category.equals(foundItem.category, ignoreCase = true)) {
            score += 30
            matchedCategory = true
        }

        // 2. Name token overlap (35%)
        val lostTokens = lostItem.itemName.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
        val foundTokens = foundItem.itemName.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }

        val common = lostTokens.filter { lt -> foundTokens.any { ft -> ft.contains(lt) || lt.contains(ft) } }
        if (common.isNotEmpty()) {
            score += Math.min(35, common.size * 18)
            matchedKeyword = true
        }

        // 3. Location match or proximity (20%)
        if (lostItem.location.isNotBlank() && foundItem.location.isNotBlank()) {
            if (lostItem.location.equals(foundItem.location, ignoreCase = true)) {
                score += 20
                matchedLocation = true
            } else if (lostItem.location.contains("Library", ignoreCase = true) && foundItem.location.contains("Library", ignoreCase = true)) {
                score += 15
                matchedLocation = true
            }
        }

        // 4. Color / Brand similarity (15%)
        if (lostItem.brand.isNotBlank() && lostItem.brand.equals(foundItem.brand, ignoreCase = true)) {
            score += 8
        }
        if (lostItem.color.isNotBlank() && lostItem.color.equals(foundItem.color, ignoreCase = true)) {
            score += 7
        }

        val finalScore = score.coerceIn(0, 100)
        return MatchResult(
            score = finalScore,
            label = getMatchLabel(finalScore),
            matchedCategory = matchedCategory,
            matchedLocation = matchedLocation,
            matchedKeyword = matchedKeyword
        )
    }

    fun getMatchLabel(score: Int): String {
        return when {
            score >= 85 -> "Very High Match"
            score >= 70 -> "High Match"
            score >= 50 -> "Possible Match"
            else -> "Low Match"
        }
    }
}
