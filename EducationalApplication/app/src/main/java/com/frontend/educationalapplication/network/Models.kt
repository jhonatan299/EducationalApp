package com.frontend.educationalapplication.network

data class AuthResponse(
    val userId: Long,
    val name: String,
    val email: String,
    val coins: Int
)

data class PomodoroResponse(
    val id: Long,
    val userId: Long,
    val subject: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val extendedMinutes: Int,
    val totalFocusMinutes: Int,
    val status: String
)

data class StudySessionResponse(
    val id: Long,
    val subject: String,
    val durationMinutes: Int,
    val source: String,
    val studyType: String?,
    val content: String?,
    val objective: String?,
    val notes: String?,
    val learned: String?,
    val difficulties: String?,
    val nextAction: String?,
    val focusRating: Int?
)

data class ReviewReminderResponse(
    val id: Long,
    val title: String,
    val subject: String,
    val dueDate: String,
    val completed: Boolean
)

data class CalendarDayResponse(
    val date: String,
    val totalMinutes: Int,
    val subjects: List<CalendarSubjectMinutes>
)

data class CalendarSubjectMinutes(
    val subject: String,
    val minutes: Int
)

data class GoalResponse(
    val id: Long,
    val title: String,
    val subject: String,
    val targetMinutes: Int,
    val currentMinutes: Int,
    val progressPercent: Double,
    val dueDate: String,
    val daysUntilDueDate: Long,
    val completed: Boolean
)

data class MissionResponse(
    val id: Long,
    val title: String,
    val description: String,
    val targetMinutes: Int,
    val progressMinutes: Int,
    val rewardCoins: Int,
    val completed: Boolean,
    val claimed: Boolean
)

data class ProgressResponse(
    val userId: Long,
    val totalMinutes: Int,
    val byDay: List<DailyStudyMinutes>,
    val bySubject: List<SubjectStudyMinutes>
)

data class DailyStudyMinutes(
    val date: String,
    val minutes: Int
)

data class SubjectStudyMinutes(
    val subject: String,
    val minutes: Int
)
