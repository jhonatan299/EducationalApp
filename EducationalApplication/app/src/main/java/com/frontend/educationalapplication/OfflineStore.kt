package com.frontend.educationalapplication

import android.content.Context
import com.frontend.educationalapplication.network.CalendarDayResponse
import com.frontend.educationalapplication.network.CalendarSubjectMinutes
import com.frontend.educationalapplication.network.DailyStudyMinutes
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.GoalResponse
import com.frontend.educationalapplication.network.MissionResponse
import com.frontend.educationalapplication.network.ProgressResponse
import com.frontend.educationalapplication.network.ReviewReminderResponse
import com.frontend.educationalapplication.network.StudySessionResponse
import com.frontend.educationalapplication.network.SubjectStudyMinutes
import org.json.JSONArray
import org.json.JSONObject

private const val OFFLINE_PREFS = "educational_offline_store"
private const val PENDING_SESSIONS_KEY = "pending_study_sessions"

data class PendingStudySessionDraft(
    val subject: String,
    val durationMinutes: Int,
    val studyType: String,
    val content: String,
    val objective: String,
    val notes: String,
    val learned: String,
    val difficulties: String,
    val nextAction: String,
    val focusRating: Int
)

fun savePendingStudySession(context: Context, draft: PendingStudySessionDraft) {
    val prefs = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE)
    val array = JSONArray(prefs.getString(PENDING_SESSIONS_KEY, "[]"))
    array.put(
        JSONObject()
            .put("subject", draft.subject)
            .put("durationMinutes", draft.durationMinutes)
            .put("studyType", draft.studyType)
            .put("content", draft.content)
            .put("objective", draft.objective)
            .put("notes", draft.notes)
            .put("learned", draft.learned)
            .put("difficulties", draft.difficulties)
            .put("nextAction", draft.nextAction)
            .put("focusRating", draft.focusRating)
    )
    prefs.edit().putString(PENDING_SESSIONS_KEY, array.toString()).apply()
}

suspend fun syncPendingStudySessions(context: Context, api: EducationalApi, userId: Long): Int {
    if (userId <= 0) return 0
    val prefs = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE)
    val array = JSONArray(prefs.getString(PENDING_SESSIONS_KEY, "[]"))
    if (array.length() == 0) return 0

    val remaining = JSONArray()
    var synced = 0
    for (index in 0 until array.length()) {
        val item = array.getJSONObject(index)
        val result = runCatching {
            api.createManualSession(
                userId = userId,
                subject = item.optString("subject", "Estudo offline"),
                durationMinutes = item.optInt("durationMinutes", 1).coerceAtLeast(1),
                studyType = item.optString("studyType", "REVIEW"),
                content = item.optString("content"),
                objective = item.optString("objective"),
                notes = item.optString("notes"),
                learned = item.optString("learned"),
                difficulties = item.optString("difficulties"),
                nextAction = item.optString("nextAction"),
                focusRating = item.optInt("focusRating", 3).coerceIn(1, 5)
            )
        }
        if (result.isSuccess) synced += 1 else remaining.put(item)
    }
    prefs.edit().putString(PENDING_SESSIONS_KEY, remaining.toString()).apply()
    return synced
}

fun cacheScreenData(context: Context, userId: Long, data: ScreenData) {
    val prefs = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE)
    prefs.edit().putString("screen_data_$userId", data.toJson().toString()).apply()
}

fun loadCachedScreenData(context: Context, userId: Long): ScreenData? {
    val raw = context.getSharedPreferences(OFFLINE_PREFS, Context.MODE_PRIVATE)
        .getString("screen_data_$userId", null) ?: return null
    return runCatching { JSONObject(raw).toScreenData() }.getOrNull()
}

private fun ScreenData.toJson() = JSONObject()
    .put("sessions", JSONArray(sessions.map { it.toJson() }))
    .put("goals", JSONArray(goals.map { it.toJson() }))
    .put("missions", JSONArray(missions.map { it.toJson() }))
    .put("reviews", JSONArray(reviews.map { it.toJson() }))
    .put("calendar", JSONArray(calendar.map { it.toJson() }))
    .put("progress", progress.toJson())

private fun JSONObject.toScreenData() = ScreenData(
    sessions = getJSONArray("sessions").objects { it.toStudySession() },
    goals = getJSONArray("goals").objects { it.toGoal() },
    missions = getJSONArray("missions").objects { it.toMission() },
    reviews = getJSONArray("reviews").objects { it.toReview() },
    calendar = getJSONArray("calendar").objects { it.toCalendarDay() },
    progress = getJSONObject("progress").toProgress()
)

private fun StudySessionResponse.toJson() = JSONObject()
    .put("id", id)
    .put("subject", subject)
    .put("durationMinutes", durationMinutes)
    .put("source", source)
    .put("studyType", studyType)
    .put("content", content)
    .put("objective", objective)
    .put("notes", notes)
    .put("learned", learned)
    .put("difficulties", difficulties)
    .put("nextAction", nextAction)
    .put("focusRating", focusRating)

private fun JSONObject.toStudySession() = StudySessionResponse(
    id = optLong("id"),
    subject = optString("subject"),
    durationMinutes = optInt("durationMinutes"),
    source = optString("source"),
    startedAt = optString("startedAt").takeIf { it.isNotBlank() },
    endedAt = optString("endedAt").takeIf { it.isNotBlank() },
    studyType = optString("studyType").takeIf { it.isNotBlank() },
    content = optString("content").takeIf { it.isNotBlank() },
    objective = optString("objective").takeIf { it.isNotBlank() },
    notes = optString("notes").takeIf { it.isNotBlank() },
    learned = optString("learned").takeIf { it.isNotBlank() },
    difficulties = optString("difficulties").takeIf { it.isNotBlank() },
    nextAction = optString("nextAction").takeIf { it.isNotBlank() },
    focusRating = if (has("focusRating") && !isNull("focusRating")) optInt("focusRating") else null,
    exerciseCount = if (has("exerciseCount") && !isNull("exerciseCount")) optInt("exerciseCount") else null,
    correctAnswers = if (has("correctAnswers") && !isNull("correctAnswers")) optInt("correctAnswers") else null
)

private fun GoalResponse.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("subject", subject)
    .put("targetMinutes", targetMinutes)
    .put("currentMinutes", currentMinutes)
    .put("progressPercent", progressPercent)
    .put("dueDate", dueDate)
    .put("daysUntilDueDate", daysUntilDueDate)
    .put("completed", completed)

private fun JSONObject.toGoal() = GoalResponse(
    id = optLong("id"),
    title = optString("title"),
    subject = optString("subject"),
    targetMinutes = optInt("targetMinutes"),
    currentMinutes = optInt("currentMinutes"),
    progressPercent = optDouble("progressPercent"),
    dueDate = optString("dueDate"),
    daysUntilDueDate = optLong("daysUntilDueDate"),
    completed = optBoolean("completed")
)

private fun MissionResponse.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("description", description)
    .put("targetMinutes", targetMinutes)
    .put("progressMinutes", progressMinutes)
    .put("rewardCoins", rewardCoins)
    .put("completed", completed)
    .put("claimed", claimed)

private fun JSONObject.toMission() = MissionResponse(
    id = optLong("id"),
    title = optString("title"),
    description = optString("description"),
    targetMinutes = optInt("targetMinutes"),
    progressMinutes = optInt("progressMinutes"),
    rewardCoins = optInt("rewardCoins"),
    completed = optBoolean("completed"),
    claimed = optBoolean("claimed")
)

private fun ReviewReminderResponse.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("subject", subject)
    .put("topic", topic)
    .put("taskTitle", taskTitle)
    .put("notes", notes)
    .put("originalDifficulty", originalDifficulty)
    .put("dueDate", dueDate)
    .put("completed", completed)
    .put("completedAt", completedAt)

private fun JSONObject.toReview() = ReviewReminderResponse(
    id = optLong("id"),
    title = optString("title"),
    subject = optString("subject"),
    topic = optString("topic").takeIf { it.isNotBlank() },
    taskTitle = optString("taskTitle").takeIf { it.isNotBlank() },
    notes = optString("notes").takeIf { it.isNotBlank() },
    originalDifficulty = optString("originalDifficulty").takeIf { it.isNotBlank() },
    dueDate = optString("dueDate"),
    completed = optBoolean("completed"),
    completedAt = optString("completedAt").takeIf { it.isNotBlank() }
)

private fun CalendarDayResponse.toJson() = JSONObject()
    .put("date", date)
    .put("totalMinutes", totalMinutes)
    .put("subjects", JSONArray(subjects.map { JSONObject().put("subject", it.subject).put("minutes", it.minutes) }))

private fun JSONObject.toCalendarDay() = CalendarDayResponse(
    date = optString("date"),
    totalMinutes = optInt("totalMinutes"),
    subjects = optJSONArray("subjects")?.objects {
        CalendarSubjectMinutes(it.optString("subject"), it.optInt("minutes"))
    }.orEmpty()
)

private fun ProgressResponse.toJson() = JSONObject()
    .put("userId", userId)
    .put("totalMinutes", totalMinutes)
    .put("byDay", JSONArray(byDay.map { JSONObject().put("date", it.date).put("minutes", it.minutes) }))
    .put("bySubject", JSONArray(bySubject.map { JSONObject().put("subject", it.subject).put("minutes", it.minutes) }))

private fun JSONObject.toProgress() = ProgressResponse(
    userId = optLong("userId"),
    totalMinutes = optInt("totalMinutes"),
    byDay = optJSONArray("byDay")?.objects { DailyStudyMinutes(it.optString("date"), it.optInt("minutes")) }.orEmpty(),
    bySubject = optJSONArray("bySubject")?.objects { SubjectStudyMinutes(it.optString("subject"), it.optInt("minutes")) }.orEmpty()
)

private fun <T> JSONArray.objects(mapper: (JSONObject) -> T): List<T> =
    List(length()) { index -> mapper(getJSONObject(index)) }
