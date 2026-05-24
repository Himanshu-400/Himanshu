package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- 1. USER PROFILE (Gamified Student, Approved Teacher, or Admin) ---
@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val name: String,
    val role: String, // "student", "teacher", "admin"
    val xp: Int = 0,
    val coins: Int = 0,
    val streak: Int = 0,
    val level: Int = 1,
    val lastActiveDate: String = "",
    val isBlocked: Boolean = false,
    val isApproved: Boolean = true // Teachers must be approved by admin
)

// --- 2. COURSE STRUCTURE ---
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Class 9", "Class 10", "Coding", "JEE", "NEET", etc.
    val instructorEmail: String,
    val instructorName: String,
    val bannerUrl: String = "",
    val lessonsJson: String = "[]", // List<Lesson> as JSON
    val isPremium: Boolean = false
)

// Lesson model contained inside Course.lessonsJson
data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val pdfUrl: String = "",
    val pdfName: String = "",
    val durationSeconds: Int = 600,
    val isBookmarked: Boolean = false
)

// --- 3. MOCK TESTS & QUIZZES ---
@Entity(tableName = "mock_tests")
data class MockTest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val durationMinutes: Int = 20,
    val questionsJson: String = "[]", // List<McqQuestion> as JSON
    val isPublished: Boolean = true
)

data class McqQuestion(
    val id: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswerIndex: Int, // 0..3 (A..D)
    val explanation: String = ""
)

// --- 4. LIVE CLASS SECURE CHANNEL ---
@Entity(tableName = "live_classes")
data class LiveClass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val teacherEmail: String,
    val teacherName: String,
    val status: String = "live", // "live", "upcoming", "replay"
    val scheduledAt: String = "",
    val watchers: Int = 120, // Live viewer count
    val chatJson: String = "[]", // List<LiveChatMessage> as JSON
    val activePollJson: String = "" // ActivePoll as JSON (if any)
)

data class LiveChatMessage(
    val senderName: String,
    val senderRole: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hasHandRaised: Boolean = false,
    val reactionEmoji: String = ""
)

data class ActivePoll(
    val question: String,
    val optionA: String,
    val optionB: String,
    val votesA: Int = 0,
    val votesB: Int = 0
)

// --- 5. SYSTEM NOTIFICATIONS ---
@Entity(tableName = "notifications")
data class StudyNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val category: String, // "live_class", "exam_alert", "new_lecture", "daily_reminder", "achievement", "omega_broadcast"
    val timestamp: Long = System.currentTimeMillis(),
    val roleTarget: String = "all" // "all", "students", "teachers"
)

// --- 6. OMEGA SYSTEM MASTER LOGS ---
@Entity(tableName = "omega_logs")
data class OmegaLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val command: String,
    val aiResponse: String,
    val timestamp: Long = System.currentTimeMillis(),
    val triggerEmail: String = "dhinwabrothers125@gmail.com"
)
