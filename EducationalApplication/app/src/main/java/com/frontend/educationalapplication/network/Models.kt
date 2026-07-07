package com.frontend.educationalapplication.network

// These data classes are similar to Java DTOs/records.
// They represent the JSON shapes returned by the Spring Boot backend.

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
    val startedAt: String?,
    val endedAt: String?,
    val studyType: String?,
    val content: String?,
    val objective: String?,
    val notes: String?,
    val learned: String?,
    val difficulties: String?,
    val nextAction: String?,
    val focusRating: Int?,
    val exerciseCount: Int?,
    val correctAnswers: Int?
)

data class ReviewReminderResponse(
    val id: Long,
    val title: String,
    val subject: String,
    val topic: String?,
    val taskTitle: String?,
    val notes: String?,
    val originalDifficulty: String?,
    val dueDate: String,
    val completed: Boolean,
    val completedAt: String?
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

data class GeneratedQuizResponse(
    val source: String,
    val gameMode: String,
    val questions: List<GeneratedQuestionResponse>
)

data class GeneratedQuestionResponse(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class SavedQuestionResponse(
    val id: Long,
    val subject: String,
    val gameMode: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuestionListResponse(
    val id: Long,
    val name: String,
    val questionCount: Long,
    val questionIds: List<Long>
)

data class RevisionRoadmapResponse(
    val options: List<StudyPlanOptionResponse>
)

data class StudyPlanOptionResponse(
    val optionNumber: Int,
    val title: String,
    val description: String,
    val estimatedTotalMinutes: Int,
    val difficulty: String,
    val suggestedTopics: List<String>,
    val revisions: List<PlannedRevisionResponse>
)

data class PlannedRevisionResponse(
    val subjectName: String,
    val topicName: String,
    val date: String,
    val time: String,
    val durationMinutes: Int,
    val revisionType: String,
    val description: String,
    val priority: String
)

data class RankingResponse(
    val period: String,
    val players: List<RankingPlayerResponse>
)

data class RankingPlayerResponse(
    val userId: Long,
    val name: String,
    val minutes: Int,
    val position: Int,
    val currentUser: Boolean
)
