package com.example.lostandfound.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.lostandfound.models.Claim
import com.example.lostandfound.models.Item
import com.example.lostandfound.models.NotificationItem
import com.example.lostandfound.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lostandfound.db"
        private const val DATABASE_VERSION = 6

        const val TABLE_USERS = "USERS"
        const val TABLE_ITEMS = "ITEMS"
        const val TABLE_CLAIMS = "CLAIMS"
        const val TABLE_NOTIFICATIONS = "NOTIFICATIONS"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                student_id TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                phone TEXT,
                department TEXT,
                profile_image TEXT,
                created_at TEXT
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        val createItemsTable = """
            CREATE TABLE $TABLE_ITEMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                type TEXT,
                item_name TEXT,
                category TEXT,
                description TEXT,
                brand TEXT,
                color TEXT,
                location TEXT,
                location_detail TEXT,
                date TEXT,
                time TEXT,
                image_uri TEXT,
                additional_info TEXT,
                verification_question TEXT,
                verification_answer TEXT,
                status TEXT,
                created_at TEXT
            )
        """.trimIndent()
        db.execSQL(createItemsTable)

        val createClaimsTable = """
            CREATE TABLE $TABLE_CLAIMS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                item_id INTEGER,
                claimant_id INTEGER,
                claimant_name TEXT,
                claimant_college_id TEXT,
                claimant_email TEXT,
                reason TEXT,
                verification_answer TEXT,
                status TEXT,
                created_at TEXT
            )
        """.trimIndent()
        db.execSQL(createClaimsTable)

        val createNotifsTable = """
            CREATE TABLE $TABLE_NOTIFICATIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                title TEXT,
                message TEXT,
                time_ago TEXT,
                type TEXT,
                is_read INTEGER DEFAULT 0,
                related_item_id INTEGER,
                created_at TEXT
            )
        """.trimIndent()
        db.execSQL(createNotifsTable)

        insertSeedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE $TABLE_CLAIMS ADD COLUMN claimant_name TEXT")
                db.execSQL("ALTER TABLE $TABLE_CLAIMS ADD COLUMN claimant_college_id TEXT")
                db.execSQL("ALTER TABLE $TABLE_CLAIMS ADD COLUMN claimant_email TEXT")
            } catch (e: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_ITEMS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_CLAIMS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
                onCreate(db)
            }
        }
    }

    private fun getDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // =========================================================================
    // USERS
    // =========================================================================
    fun registerUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", user.name)
            put("student_id", user.studentId)
            put("email", user.email)
            put("password", user.password)
            put("phone", user.phone)
            put("department", user.department)
            put("profile_image", user.profileImage)
            put("created_at", getDateTime())
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun loginUser(emailOrId: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS, null,
            "(email = ? OR student_id = ?) AND password = ?",
            arrayOf(emailOrId, emailOrId, password),
            null, null, null
        )

        return cursor?.use {
            if (it.moveToFirst()) parseUser(it) else null
        }
    }

    fun getUserById(id: Int): User? {
        val db = readableDatabase
        val cursor = db.query(TABLE_USERS, null, "id = ?", arrayOf(id.toString()), null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) parseUser(it) else null
        }
    }

    fun updateUser(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", user.name)
            put("student_id", user.studentId)
            put("email", user.email)
            put("phone", user.phone)
            put("department", user.department)
            put("profile_image", user.profileImage)
        }
        return db.update(TABLE_USERS, values, "id = ?", arrayOf(user.id.toString()))
    }

    private fun parseUser(cursor: Cursor): User {
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")),
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
            password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
            phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")) ?: "",
            department = cursor.getString(cursor.getColumnIndexOrThrow("department")) ?: "",
            profileImage = cursor.getString(cursor.getColumnIndexOrThrow("profile_image")) ?: ""
        )
    }

    // =========================================================================
    // ITEMS
    // =========================================================================
    fun addItem(item: Item): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", item.userId)
            put("type", item.type)
            put("item_name", item.itemName)
            put("category", item.category)
            put("description", item.description)
            put("brand", item.brand)
            put("color", item.color)
            put("location", item.location)
            put("location_detail", item.locationDetail)
            put("date", item.date)
            put("time", item.time)
            put("image_uri", item.imageUri)
            put("additional_info", item.additionalInfo)
            put("verification_question", item.verificationQuestion)
            put("verification_answer", item.verificationAnswer)
            put("status", item.status)
            put("created_at", getDateTime())
        }
        return db.insert(TABLE_ITEMS, null, values)
    }

    fun getItemById(id: Int): Item? {
        val db = readableDatabase
        val cursor = db.query(TABLE_ITEMS, null, "id = ?", arrayOf(id.toString()), null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) parseItem(it) else null
        }
    }

    fun getAllItems(): List<Item> = getItems("1=1", null)

    fun getRecentItems(limit: Int = 10): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.query(TABLE_ITEMS, null, null, null, null, null, "id DESC", limit.toString())
        cursor?.use {
            while (it.moveToNext()) {
                items.add(parseItem(it))
            }
        }
        return items
    }

    fun getItemsByType(type: String): List<Item> {
        return getItems("type = ?", arrayOf(type))
    }

    fun getMyReports(userId: Int, filter: String = "ALL"): List<Item> {
        return when (filter) {
            "LOST" -> getItems("user_id = ? AND type = ?", arrayOf(userId.toString(), "LOST"))
            "FOUND" -> getItems("user_id = ? AND type = ?", arrayOf(userId.toString(), "FOUND"))
            "RESOLVED" -> getItems("user_id = ? AND status = ?", arrayOf(userId.toString(), "Resolved"))
            else -> getItems("user_id = ?", arrayOf(userId.toString()))
        }
    }

    fun searchAndFilterItems(query: String?, type: String?, category: String?, location: String?): List<Item> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!type.isNullOrEmpty() && type != "ALL") {
            conditions.add("type = ?")
            args.add(type)
        }

        if (!category.isNullOrEmpty() && category != "All") {
            conditions.add("category = ?")
            args.add(category)
        }

        if (!location.isNullOrEmpty() && location != "All") {
            conditions.add("location LIKE ?")
            args.add("%$location%")
        }

        if (!query.isNullOrBlank()) {
            conditions.add("(item_name LIKE ? OR description LIKE ? OR location LIKE ?)")
            val wildcard = "%${query.trim()}%"
            args.add(wildcard)
            args.add(wildcard)
            args.add(wildcard)
        }

        val selection = if (conditions.isEmpty()) null else conditions.joinToString(" AND ")
        val selectionArgs = if (args.isEmpty()) null else args.toTypedArray()

        return getItems(selection, selectionArgs)
    }

    private fun getItems(selection: String?, selectionArgs: Array<String>?): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.query(TABLE_ITEMS, null, selection, selectionArgs, null, null, "id DESC")
        cursor?.use {
            while (it.moveToNext()) {
                items.add(parseItem(it))
            }
        }
        return items
    }

    private fun parseItem(cursor: Cursor): Item {
        return Item(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
            type = cursor.getString(cursor.getColumnIndexOrThrow("type")) ?: "LOST",
            itemName = cursor.getString(cursor.getColumnIndexOrThrow("item_name")) ?: "",
            category = cursor.getString(cursor.getColumnIndexOrThrow("category")) ?: "",
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
            brand = cursor.getString(cursor.getColumnIndexOrThrow("brand")) ?: "",
            color = cursor.getString(cursor.getColumnIndexOrThrow("color")) ?: "",
            location = cursor.getString(cursor.getColumnIndexOrThrow("location")) ?: "",
            locationDetail = cursor.getString(cursor.getColumnIndexOrThrow("location_detail")) ?: "",
            date = cursor.getString(cursor.getColumnIndexOrThrow("date")) ?: "",
            time = cursor.getString(cursor.getColumnIndexOrThrow("time")) ?: "",
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri")) ?: "",
            additionalInfo = cursor.getString(cursor.getColumnIndexOrThrow("additional_info")) ?: "",
            verificationQuestion = cursor.getString(cursor.getColumnIndexOrThrow("verification_question")) ?: "",
            verificationAnswer = cursor.getString(cursor.getColumnIndexOrThrow("verification_answer")) ?: "",
            status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "Active",
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")) ?: ""
        )
    }

    fun updateItemStatus(itemId: Int, status: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", status)
        }
        db.update(TABLE_ITEMS, values, "id = ?", arrayOf(itemId.toString()))
    }

    fun deleteItem(itemId: Int) {
        val db = writableDatabase
        db.delete(TABLE_ITEMS, "id = ?", arrayOf(itemId.toString()))
    }

    // =========================================================================
    // CLAIMS
    // =========================================================================
    fun addClaim(claim: Claim): Long {
        val db = writableDatabase
        val claimantUser = getUserById(claim.claimantId)
        val claimantName = if (claim.claimantName.isNotBlank()) claim.claimantName else (claimantUser?.name ?: "")
        val claimantCollegeId = if (claim.claimantCollegeId.isNotBlank()) claim.claimantCollegeId else (claimantUser?.studentId ?: "")
        val claimantEmail = if (claim.claimantEmail.isNotBlank()) claim.claimantEmail else (claimantUser?.email ?: "")

        val values = ContentValues().apply {
            put("item_id", claim.itemId)
            put("claimant_id", claim.claimantId)
            put("claimant_name", claimantName)
            put("claimant_college_id", claimantCollegeId)
            put("claimant_email", claimantEmail)
            put("reason", claim.reason)
            put("verification_answer", claim.verificationAnswer)
            put("status", if (claim.status.isNotBlank()) claim.status else "PENDING")
            put("created_at", getDateTime())
        }
        return db.insert(TABLE_CLAIMS, null, values)
    }

    fun getClaimById(id: Int): Claim? {
        val db = readableDatabase
        val cursor = db.query(TABLE_CLAIMS, null, "id = ?", arrayOf(id.toString()), null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) parseClaim(it) else null
        }
    }

    fun getClaimsByItemId(itemId: Int): List<Claim> {
        val claims = mutableListOf<Claim>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CLAIMS, null, "item_id = ?", arrayOf(itemId.toString()), null, null, "id DESC")
        cursor?.use {
            while (it.moveToNext()) {
                claims.add(parseClaim(it))
            }
        }
        return claims
    }

    fun getPendingClaimForItem(itemId: Int): Claim? {
        val db = readableDatabase
        val cursor = db.query(TABLE_CLAIMS, null, "item_id = ? AND status = 'PENDING'", arrayOf(itemId.toString()), null, null, "id DESC", "1")
        return cursor?.use {
            if (it.moveToFirst()) parseClaim(it) else null
        }
    }

    fun getClaimsByClaimantId(claimantId: Int): List<Claim> {
        val claims = mutableListOf<Claim>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CLAIMS, null, "claimant_id = ?", arrayOf(claimantId.toString()), null, null, "id DESC")
        cursor?.use {
            while (it.moveToNext()) {
                claims.add(parseClaim(it))
            }
        }
        return claims
    }

    fun updateClaimStatus(claimId: Int, status: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("status", status)
        }
        return db.update(TABLE_CLAIMS, values, "id = ?", arrayOf(claimId.toString()))
    }

    private fun parseClaim(cursor: Cursor): Claim {
        val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        val itemId = cursor.getInt(cursor.getColumnIndexOrThrow("item_id"))
        val claimantId = cursor.getInt(cursor.getColumnIndexOrThrow("claimant_id"))
        val reason = cursor.getString(cursor.getColumnIndexOrThrow("reason")) ?: ""
        val verificationAnswer = cursor.getString(cursor.getColumnIndexOrThrow("verification_answer")) ?: ""
        val status = cursor.getString(cursor.getColumnIndexOrThrow("status")) ?: "PENDING"
        val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")) ?: ""

        val nameCol = cursor.getColumnIndex("claimant_name")
        val collegeIdCol = cursor.getColumnIndex("claimant_college_id")
        val emailCol = cursor.getColumnIndex("claimant_email")

        var cName = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
        var cCollegeId = if (collegeIdCol != -1) cursor.getString(collegeIdCol) ?: "" else ""
        var cEmail = if (emailCol != -1) cursor.getString(emailCol) ?: "" else ""

        // Always resolve via claimant user ID if empty or to ensure accuracy
        if (cCollegeId.isEmpty() || cName.isEmpty() || cEmail.isEmpty()) {
            val user = getUserById(claimantId)
            if (user != null) {
                if (cName.isEmpty()) cName = user.name
                if (cCollegeId.isEmpty()) cCollegeId = user.studentId
                if (cEmail.isEmpty()) cEmail = user.email
            }
        }

        val item = getItemById(itemId)
        val itemName = item?.itemName ?: ""

        return Claim(
            id = id,
            itemId = itemId,
            claimantId = claimantId,
            claimantName = cName,
            claimantCollegeId = cCollegeId,
            claimantEmail = cEmail,
            reason = reason,
            verificationAnswer = verificationAnswer,
            status = status,
            createdAt = createdAt,
            itemName = itemName
        )
    }

    // =========================================================================
    // NOTIFICATIONS
    // =========================================================================
    fun getNotifications(userId: Int): List<NotificationItem> {
        val notifs = mutableListOf<NotificationItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NOTIFICATIONS, null, "user_id = ? OR user_id = 0", arrayOf(userId.toString()), null, null, "id DESC")
        cursor?.use {
            while (it.moveToNext()) {
                notifs.add(
                    NotificationItem(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        message = it.getString(it.getColumnIndexOrThrow("message")),
                        timeAgo = it.getString(it.getColumnIndexOrThrow("time_ago")),
                        type = it.getString(it.getColumnIndexOrThrow("type")),
                        isRead = it.getInt(it.getColumnIndexOrThrow("is_read")) == 1,
                        relatedItemId = it.getInt(it.getColumnIndexOrThrow("related_item_id"))
                    )
                )
            }
        }
        return notifs
    }

    fun addNotification(userId: Int, title: String, message: String, type: String, timeAgo: String = "Just now", relatedItemId: Int? = null): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("title", title)
            put("message", message)
            put("type", type)
            put("time_ago", timeAgo)
            put("is_read", 0)
            put("related_item_id", relatedItemId ?: 0)
            put("created_at", getDateTime())
        }
        return db.insert(TABLE_NOTIFICATIONS, null, values)
    }

    fun markAllNotificationsRead(userId: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        db.update(TABLE_NOTIFICATIONS, values, "user_id = ? OR user_id = 0", arrayOf(userId.toString()))
    }

    // =========================================================================
    // STATS
    // =========================================================================
    fun getDashboardStatistics(): IntArray {
        val db = readableDatabase
        var lost = 0
        var found = 0
        var claims = 0
        var returned = 0

        val c1 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE type = 'LOST'", null)
        if (c1.moveToFirst()) lost = c1.getInt(0)
        c1.close()

        val c2 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE type = 'FOUND'", null)
        if (c2.moveToFirst()) found = c2.getInt(0)
        c2.close()

        val c3 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CLAIMS", null)
        if (c3.moveToFirst()) claims = c3.getInt(0)
        c3.close()

        val c4 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE status = 'Resolved'", null)
        if (c4.moveToFirst()) returned = c4.getInt(0)
        c4.close()

        return intArrayOf(lost, found, claims, returned)
    }

    fun getUserStats(userId: Int): Triple<Int, Int, Int> {
        val db = readableDatabase
        var posted = 0
        var found = 0
        var returned = 0

        val c1 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE user_id = ?", arrayOf(userId.toString()))
        if (c1.moveToFirst()) posted = c1.getInt(0)
        c1.close()

        val c2 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE user_id = ? AND type = 'FOUND'", arrayOf(userId.toString()))
        if (c2.moveToFirst()) found = c2.getInt(0)
        c2.close()

        val c3 = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE user_id = ? AND status = 'Resolved'", arrayOf(userId.toString()))
        if (c3.moveToFirst()) returned = c3.getInt(0)
        c3.close()

        return Triple(posted, found, returned)
    }

    fun getLostItems(): List<Item> = getItemsByType("LOST")
    fun getFoundItems(): List<Item> = getItemsByType("FOUND")

    fun getUnreadNotificationCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NOTIFICATIONS WHERE (user_id = ? OR user_id = 0) AND is_read = 0", arrayOf(userId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getCategoriesWithCounts(): List<com.example.lostandfound.models.Category> {
        val categoryTemplates = listOf(
            Pair("Electronics", "💻"),
            Pair("Keys", "🔑"),
            Pair("ID Cards", "🪪"),
            Pair("Bags", "🎒"),
            Pair("Documents", "📄"),
            Pair("Clothing", "👕"),
            Pair("Books", "📚"),
            Pair("Other", "📦")
        )

        val db = readableDatabase
        val result = mutableListOf<com.example.lostandfound.models.Category>()

        var totalItems = 0
        val cAll = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS", null)
        if (cAll.moveToFirst()) totalItems = cAll.getInt(0)
        cAll.close()
        result.add(com.example.lostandfound.models.Category("All", "🔍", totalItems, true))

        for ((name, emoji) in categoryTemplates) {
            var count = 0
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ITEMS WHERE category LIKE ?", arrayOf("%$name%"))
            if (cursor.moveToFirst()) count = cursor.getInt(0)
            cursor.close()
            result.add(com.example.lostandfound.models.Category(name, emoji, count, false))
        }

        return result
    }

    fun getAllLocations(): List<String> {
        val list = mutableListOf("All", "Central Library", "Main Cafeteria", "Computer Center", "Hostel Area", "Main Auditorium", "Sports Complex", "Classroom Complex")
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT location FROM $TABLE_ITEMS WHERE location IS NOT NULL AND location != ''", null)
        cursor?.use {
            while (it.moveToNext()) {
                val loc = it.getString(0)
                if (!list.contains(loc)) {
                    list.add(loc)
                }
            }
        }
        return list
    }

    fun findPotentialMatches(item: Item): List<Pair<Item, com.example.lostandfound.utils.MatchUtils.MatchResult>> {
        val oppositeType = if (item.type.equals("LOST", ignoreCase = true)) "FOUND" else "LOST"
        val candidates = getItemsByType(oppositeType)
        val matchedList = mutableListOf<Pair<Item, com.example.lostandfound.utils.MatchUtils.MatchResult>>()

        for (candidate in candidates) {
            val result = if (item.type.equals("LOST", ignoreCase = true)) {
                com.example.lostandfound.utils.MatchUtils.calculateMatch(item, candidate)
            } else {
                com.example.lostandfound.utils.MatchUtils.calculateMatch(candidate, item)
            }
            if (result.score >= 40) {
                matchedList.add(Pair(candidate, result))
            }
        }

        matchedList.sortByDescending { it.second.score }
        return matchedList
    }

    // =========================================================================
    // SEED DATA
    // =========================================================================
    private fun insertSeedData(db: SQLiteDatabase) {
        // User 1: Jainik Bhandari (Main student account)
        val u1 = ContentValues().apply {
            put("name", "Jainik Bhandari")
            put("student_id", "22011011001")
            put("email", "jainik.bhandari@gcet.ac.in")
            put("password", "password123")
            put("phone", "+91 98250 12345")
            put("department", "Computer Science & Engineering")
            put("created_at", getDateTime())
        }
        db.insert(TABLE_USERS, null, u1)

        // User 2: Aarav Sharma
        val u2 = ContentValues().apply {
            put("name", "Aarav Sharma")
            put("student_id", "22011011015")
            put("email", "aarav.sharma@gcet.ac.in")
            put("password", "password123")
            put("phone", "+91 98765 43210")
            put("department", "Electronics & Communication")
            put("created_at", getDateTime())
        }
        db.insert(TABLE_USERS, null, u2)

        // User 3: GCET Campus Security Desk
        val u3 = ContentValues().apply {
            put("name", "GCET Security Desk")
            put("student_id", "GCET-STAFF-01")
            put("email", "security@gcet.ac.in")
            put("password", "security123")
            put("phone", "+91 2692 231651")
            put("department", "GCET Campus Security Desk")
            put("created_at", getDateTime())
        }
        db.insert(TABLE_USERS, null, u3)

        // Insert Items
        val items = listOf(
            // Item 1: Lost AirPods Pro
            ContentValues().apply {
                put("user_id", 1)
                put("type", "LOST")
                put("item_name", "Apple AirPods Pro (2nd Gen)")
                put("category", "Electronics")
                put("brand", "Apple")
                put("color", "White")
                put("location", "GCET Central Library")
                put("location_detail", "2nd Floor study desk #42")
                put("date", "2026-09-28")
                put("time", "14:30")
                put("description", "White wireless earbuds in transparent Spigen case. Left pod has tiny hairline scratch.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_airpods")
                put("additional_info", "SAFE_HANDOVER")
                put("verification_question", "What is written on the small keychain tag attached to the case?")
                put("status", "Potential Match")
                put("created_at", getDateTime())
            },
            // Item 2: Found AirPods
            ContentValues().apply {
                put("user_id", 2)
                put("type", "FOUND")
                put("item_name", "White Apple AirPods with Case")
                put("category", "Electronics")
                put("brand", "Apple")
                put("color", "White")
                put("location", "GCET Central Library")
                put("location_detail", "Circulation desk magazine area")
                put("date", "2026-09-28")
                put("time", "16:00")
                put("description", "Found white earbuds in clear case. Deposited with GCET Security Desk Gate #1.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_airpods")
                put("additional_info", "SAFE_HANDOVER")
                put("verification_question", "What is written on the small keychain tag attached to the case?")
                put("status", "Potential Match")
                put("created_at", getDateTime())
            },
            // Item 3: Lost Calculator
            ContentValues().apply {
                put("user_id", 1)
                put("type", "LOST")
                put("item_name", "Casio FX-991EX Scientific Calculator")
                put("category", "Electronics")
                put("brand", "Casio")
                put("color", "Black/White")
                put("location", "GCET Computer Center")
                put("location_detail", "Lab 3 Terminal 18")
                put("date", "2026-09-27")
                put("time", "11:15")
                put("description", "Scientific calculator with slide cover. 'GCET-CP-22' written on the back.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_calculator")
                put("additional_info", "SAFE_HANDOVER")
                put("verification_question", "What 4-digit code is etched inside the slide cover?")
                put("status", "Active")
                put("created_at", getDateTime())
            },
            // Item 4: Found Keys
            ContentValues().apply {
                put("user_id", 3)
                put("type", "FOUND")
                put("item_name", "Hostel Room Keys (Bunch of 3)")
                put("category", "Keys")
                put("brand", "Godrej")
                put("color", "Brass/Silver")
                put("location", "GCET Hostel Block")
                put("location_detail", "Hostel Block A staircase")
                put("date", "2026-09-28")
                put("time", "19:00")
                put("description", "Three keys on Captain America shield rubber keychain. Tagged with Room 304.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_keys")
                put("additional_info", "SAFE_HANDOVER")
                put("verification_question", "What is the small secondary key used for?")
                put("status", "Active")
                put("created_at", getDateTime())
            },
            // Item 5: Found Student ID Card
            ContentValues().apply {
                put("user_id", 3)
                put("type", "FOUND")
                put("item_name", "Student Identity Card & Smart Pass")
                put("category", "ID Cards")
                put("brand", "GCET")
                put("color", "Blue/White")
                put("location", "GCET Canteen")
                put("location_detail", "Table near juice counter")
                put("date", "2026-09-29")
                put("time", "09:10")
                put("description", "ID card in navy GCET college lanyard. Belongs to Electrical Engineering department.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_id_card")
                put("additional_info", "SAFE_HANDOVER")
                put("verification_question", "What is the student enrollment/roll number?")
                put("status", "Active")
                put("created_at", getDateTime())
            },
            // Item 6: Lost Backpack
            ContentValues().apply {
                put("user_id", 1)
                put("type", "LOST")
                put("item_name", "Samsonite College Backpack")
                put("category", "Bags")
                put("brand", "Samsonite")
                put("color", "Navy Blue")
                put("location", "GCET Central Auditorium")
                put("location_detail", "Row G Seat 14")
                put("date", "2026-09-26")
                put("time", "17:30")
                put("description", "Navy blue backpack containing Engineering Mathematics spiral notebooks.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_backpack")
                put("additional_info", "SAFE_HANDOVER")
                put("verification_question", "What brand is the bottle inside the side net?")
                put("status", "Claim Pending")
                put("created_at", getDateTime())
            },
            // Item 7: Resolved Wallet
            ContentValues().apply {
                put("user_id", 1)
                put("type", "LOST")
                put("item_name", "Black Leather Bi-Fold Wallet")
                put("category", "Documents")
                put("brand", "Woodland")
                put("color", "Black")
                put("location", "Main Cafeteria")
                put("date", "2026-09-24")
                put("description", "Contained driving license and metro smart card.")
                put("image_uri", "android.resource://com.example.lostandfound/drawable/img_wallet")
                put("status", "Resolved")
                put("created_at", getDateTime())
            }
        )

        for (item in items) {
            db.insert(TABLE_ITEMS, null, item)
        }

        // Insert Notifications
        val notifs = listOf(
            ContentValues().apply {
                put("user_id", 1)
                put("title", "Possible Match Found! 🔎")
                put("message", "A possible match was found for your lost 'Apple AirPods Pro' at Central Library.")
                put("time_ago", "15m ago")
                put("type", "match")
                put("is_read", 0)
                put("related_item_id", 2)
                put("created_at", getDateTime())
            },
            ContentValues().apply {
                put("user_id", 1)
                put("title", "Claim Status Update 📋")
                put("message", "Your ownership claim for 'Samsonite Backpack' is under review by Security.")
                put("time_ago", "2h ago")
                put("type", "claim")
                put("is_read", 0)
                put("related_item_id", 6)
                put("created_at", getDateTime())
            },
            ContentValues().apply {
                put("user_id", 1)
                put("title", "Item Returned! 🤝")
                put("message", "Your item 'Black Leather Bi-Fold Wallet' has been marked as returned.")
                put("time_ago", "2d ago")
                put("type", "returned")
                put("is_read", 1)
                put("related_item_id", 7)
                put("created_at", getDateTime())
            },
            ContentValues().apply {
                put("user_id", 0)
                put("title", "Campus Security Notice 🛡️")
                put("message", "Unclaimed property depository is open Monday-Friday 10 AM to 5 PM at Main Gate.")
                put("time_ago", "3d ago")
                put("type", "system")
                put("is_read", 1)
                put("created_at", getDateTime())
            }
        )

        for (notif in notifs) {
            db.insert(TABLE_NOTIFICATIONS, null, notif)
        }
    }
}
