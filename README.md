# CampusFind (FindIT) 🎓📱
### Campus Lost & Found System for Universities & Colleges

A native Android application built with **Kotlin**, **XML**, **Material Design Components**, and **SQLite Database**. Designed to simplify and secure the process of reporting, discovering, claiming, and returning lost belongings across campus.

---

## 🌟 Key Features

### 1. 📋 Complete Ownership Claim Flow
* **Verification Challenge**: Finders can attach security questions to posted items that only the true owner would know.
* **Claim Submission**: Claimants submit proof, their answer to the verification challenge, phone number, and agree to physical College ID verification.
* **Persistent Record**: Automatically saves Claimant Name, College ID, Email, Verification Answer, and Status in SQLite.
* **Claim Details Screen**: Dedicated screen displaying full item summary, claimant identity (fetched via Claimant User ID), verification answer quote, 4-digit handover PIN, and safe campus handover tips.
* **Approve & Reject Actions**:
  * **Approve**: Updates item/claim status and notifies the claimant to meet at a safe campus location with their College ID and Handover PIN.
  * **Reject**: Reverts item to active state and notifies the claimant without deleting claim history.

### 2. 🔔 Real-time Notification System
* **Detailed Finder Alerts**: When a claim is filed, the finder receives an in-app notification showing:
  * Claimant Name
  * College ID
  * Email Address
  * Submitted Verification Answer
  * Claim Status
* **Status Updates**: Instant notifications for match alerts, claim approvals, rejections, and resolved items.
* **Direct Navigation**: Clicking any claim notification opens the **Claim Details Screen** immediately.

### 3. 🛡️ Safe Campus Handover Shield & 4-Digit PIN
* **4-Digit Handover PIN**: Generated per item to serve as a cryptographic verification step before physical transfer.
* **Physical College ID Matching**: Enforces presenting official student identity cards during in-person handovers.
* **Designated Safe Locations**: Recommends public and secure campus meeting points (e.g., GCET Central Library, Campus Security Desk Gate #1).

### 4. 🧠 Smart Matching Engine
* Evaluates similarity between lost and found item reports using token matching, category filters, and campus location proximity.
* Automatically notifies users when a potential match exceeds the threshold.

### 5. 🔍 Search & Exploration
* Full-text search across item names, descriptions, and locations.
* Quick category filter chips (Electronics, Keys, ID Cards, Bags, Documents, Books, etc.) with dynamic item counts.
* Filter by Lost vs. Found types and specific campus facilities.

### 6. 👤 User Authentication & Profiles
* Secure registration and login flow using College ID or Email.
* Profile management, department tagging, and user statistics (Items Posted, Items Found, Items Returned).

---

## 🛠️ Technology Stack

* **Language**: Kotlin 1.9.0
* **Platform**: Native Android (Min SDK: 24 / Target SDK: 34)
* **Architecture**: MVVM / Activity & Fragment Architecture
* **View System**: Android XML Layouts with ViewBinding
* **UI Components**: Material Components (`MaterialCardView`, `MaterialButton`, `TextInputLayout`, Bottom Navigation)
* **Local Database**: Native Android SQLite (`SQLiteOpenHelper`)
* **Build System**: Gradle 8.1.0

---

## 📂 Project Structure

```
LostAndFound/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/example/lostandfound/
│   │       │   ├── LostAndFoundApp.kt
│   │       │   ├── activities/
│   │       │   │   ├── SplashActivity.kt
│   │       │   │   ├── LoginActivity.kt
│   │       │   │   ├── RegisterActivity.kt
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── ItemDetailsActivity.kt
│   │       │   │   ├── ClaimDetailsActivity.kt       <-- Claim Details screen
│   │       │   │   ├── SmartMatchActivity.kt
│   │       │   │   ├── NotificationsActivity.kt
│   │       │   │   └── EditProfileActivity.kt
│   │       │   ├── adapters/
│   │       │   │   ├── ItemAdapter.kt
│   │       │   │   ├── CategoryAdapter.kt
│   │       │   │   ├── MyReportAdapter.kt
│   │       │   │   └── NotificationAdapter.kt
│   │       │   ├── database/
│   │       │   │   └── DatabaseHelper.kt             <-- SQLite tables & CRUD
│   │       │   ├── fragments/
│   │       │   │   ├── HomeFragment.kt
│   │       │   │   ├── ExploreFragment.kt
│   │       │   │   ├── ReportItemFragment.kt
│   │       │   │   ├── MyReportsFragment.kt
│   │       │   │   └── ProfileFragment.kt
│   │       │   ├── models/
│   │       │   │   ├── User.kt
│   │       │   │   ├── Item.kt
│   │       │   │   ├── Claim.kt                      <-- Complete claim model
│   │       │   │   ├── NotificationItem.kt
│   │       │   │   └── Category.kt
│   │       │   └── utils/
│   │       │       ├── SessionManager.kt
│   │       │       ├── MatchUtils.kt
│   │       │       └── ImageUtils.kt
│   │       └── res/
│   │           ├── layout/
│   │           │   ├── activity_claim_details.xml    <-- Claim Details layout
│   │           │   ├── activity_item_details.xml
│   │           │   ├── activity_notifications.xml
│   │           │   ├── dialog_claim.xml
│   │           │   └── ...
│   │           ├── values/
│   │           └── drawable/
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── .gitignore
```

---

## 👥 Seed Test Accounts

The SQLite database automatically initializes with sample accounts and data for multi-user testing:

| User | College ID | Email | Password | Role |
| :--- | :--- | :--- | :--- | :--- |
| **Jainik Bhandari** | `22011011001` | `jainik.bhandari@gcet.ac.in` | `password123` | Student (CSE) |
| **Aarav Sharma** | `22011011015` | `aarav.sharma@gcet.ac.in` | `password123` | Student (ECE) |
| **GCET Security Desk** | `GCET-STAFF-01` | `security@gcet.ac.in` | `security123` | Campus Staff |

---

## 🚀 How to Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/campusfind-lost-and-found.git
   ```
2. **Open in Android Studio**:
   * Select **Open an Existing Project** and navigate to the project directory.
   * Allow Gradle to sync.
3. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```
4. **Deploy & Test**:
   * Run on an Android Emulator or connected physical device (API 24+).
   * Log in with User 1 or User 2 to test the claim lifecycle, notification dispatch, and handover PIN verification!
