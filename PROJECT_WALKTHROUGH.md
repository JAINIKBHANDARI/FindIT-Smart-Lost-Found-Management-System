# CampusFind (FindIT) - Project & Testing Walkthrough 📋

This guide provides an end-to-end testing and verification walkthrough for the **CampusFind (FindIT)** Android Application.

---

## 🧪 Testing the Complete Claim & Notification Flow with Two Accounts

### Test Accounts Available (Pre-seeded in SQLite)
* **Account 1 (Owner/Finder)**:
  * Name: `Aarav Sharma`
  * College ID: `22011011015`
  * Email: `aarav.sharma@gcet.ac.in`
  * Password: `password123`
  * Posted Item: Found Item #2: *"White Apple AirPods with Case"* (Location: *GCET Central Library*)
    * Verification Question: *"What is written on the small keychain tag attached to the case?"*
* **Account 2 (Claimant)**:
  * Name: `Jainik Bhandari`
  * College ID: `22011011001`
  * Email: `jainik.bhandari@gcet.ac.in`
  * Password: `password123`

---

## 🔄 Step-by-Step Verification Lifecycle

### Step 1: Login as Claimant (Jainik Bhandari)
1. Open the app and log in with:
   * Email/ID: `jainik.bhandari@gcet.ac.in`
   * Password: `password123`
2. Go to **Explore** or search for *"AirPods"*.
3. Open the item details for *"White Apple AirPods with Case"* reported by Aarav Sharma.

### Step 2: Submit an Ownership Claim
1. Tap the **"Claim Item"** button at the bottom.
2. In the Claim dialog:
   * Notice the finder's verification question is displayed: *"What is written on the small keychain tag attached to the case?"*
   * Enter your answer (e.g., *"Initials JB in blue ink"*).
   * Enter distinguishing details/proof (e.g., *"Lost on Monday afternoon while reading in desk #42"*).
   * Notice the **GCET Student ID** (`22011011001`) and Phone are populated from your profile.
   * Check the agreement to present your physical GCET College ID during handover.
3. Tap **Submit**.
4. The item status transitions to **"Claim Pending"**, and the database records:
   * Claimant Name: `Jainik Bhandari`
   * College ID: `22011011001` (fetched via Claimant User ID)
   * Email: `jainik.bhandari@gcet.ac.in`
   * Verification Answer: `Initials JB in blue ink`
   * Status: `PENDING`

---

### Step 3: Login as Finder (Aarav Sharma)
1. Log out via Profile tab -> **Logout**.
2. Log in as Aarav Sharma:
   * Email/ID: `aarav.sharma@gcet.ac.in`
   * Password: `password123`
3. Tap the **Notifications 🔔** bell icon on the top header.
4. **Notice the detailed finder notification**:
   ```
   Title: New Claim Request: White Apple AirPods with Case 📋
   Message:
   Claimant: Jainik Bhandari
   College ID: 22011011001
   Email: jainik.bhandari@gcet.ac.in
   Answer: Initials JB in blue ink
   Status: PENDING
   ```

---

### Step 4: Open Claim Details & Verify
1. Tap the notification in the Notifications screen (or open the item from *My Reports* and tap *"Review Claim Details 📋"*).
2. The **Claim Details Screen** opens showing:
   * Claimed Item summary & Finder challenge question.
   * **Claimant Identity**:
     * Full Name: `Jainik Bhandari`
     * College ID: `22011011001` (retrieved via Claimant User ID)
     * Email: `jainik.bhandari@gcet.ac.in`
   * **Submitted Verification Answer**: *"Initials JB in blue ink"*.
   * Additional details & notes.
   * **4-Digit Handover PIN**: Hidden by default (`PIN: #••••`), toggleable with *"View PIN"*.
   * **Safe Campus Handover Shield**: Guidance recommending public handover spots like *GCET Central Library Desk* or *Security Desk Gate #1*.

---

### Step 5: Action Options

#### Option A: Approve Claim ✓
1. Tap **Approve Claim ✓**.
2. Confirm the approval dialog.
3. Effects:
   * Claim status updates to **APPROVED**.
   * Item status updates to **Resolved**.
   * Claimant (`Jainik Bhandari`) receives a notification:
     ```
     Title: Claim Approved! 🎉 Meet for Handover
     Message: Your claim for 'White Apple AirPods with Case' was approved! Please meet at a safe campus location (GCET Central Library) with your College ID (22011011001) and Handover PIN: #<4-digit PIN>.
     ```

#### Option B: Reject Claim ✕
1. Tap **Reject Claim ✕**.
2. Confirm the rejection dialog.
3. Effects:
   * Claim status updates to **REJECTED**.
   * Claim record and audit history are **retained** in SQLite (never deleted).
   * Item status returns to **Active**.
   * Claimant (`Jainik Bhandari`) receives a notification:
     ```
     Title: Claim Rejected ❌
     Message: Your claim for 'White Apple AirPods with Case' could not be verified by the finder. History has been preserved. If you have questions, please visit GCET Campus Security Desk.
     ```

---

## 🛠️ Verification & Compilation Checklist
- [x] Kotlin code compiled with 0 errors via Gradle (`./gradlew assembleDebug`).
- [x] SQLite schema upgraded to Version 6 with dedicated `claimant_name`, `claimant_college_id`, and `claimant_email` columns.
- [x] Claimant User ID used to fetch and display College ID.
- [x] Finder receives detailed notification showing name, college ID, email, verification answer, and status.
- [x] Dedicated `ClaimDetailsActivity` created with Approve and Reject actions.
- [x] Approval notifies claimant to meet at safe campus location with College ID and Handover PIN.
- [x] Rejection notifies claimant without deleting history.
- [x] Existing 4-digit handover PIN maintained as verification step.
- [x] Git ignore configured and temporary files cleaned for GitHub push.
