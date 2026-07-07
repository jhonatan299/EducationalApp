package com.frontend.educationalapplication

import android.content.Context
import com.frontend.educationalapplication.network.StudySessionResponse
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val MISSION_PREFS = "mission_gamification_store"
private const val MIN_VALID_SESSION_MINUTES = 10

data class AppMissionUi(
    val id: String,
    val name: String,
    val description: String,
    val progressText: String,
    val progress: Float,
    val rewardPoints: Int,
    val status: String
)

data class CustomGoalUi(
    val id: Long,
    val title: String,
    val description: String,
    val period: String,
    val progressText: String,
    val progress: Float,
    val completed: Boolean
)

data class UserPointsUi(
    val officialPoints: Int,
    val totalPoints: Int,
    val pointsToday: Int,
    val currentStudyStreak: Int,
    val weeklyStreak: Int
)

data class MissionGamificationState(
    val missions: List<AppMissionUi>,
    val customGoals: List<CustomGoalUi>,
    val points: UserPointsUi
)

private data class MissionStudyEvent(
    val id: String,
    val date: String,
    val timestamp: Long,
    val durationMinutes: Int,
    val subject: String
)

private data class StoredCustomGoal(
    val id: Long,
    val title: String,
    val description: String,
    val period: String,
    val targetValue: Int,
    val createdAt: Long
)

private data class PointHistoryEntry(
    val id: Long,
    val userId: Long,
    val sourceType: String,
    val sourceId: String,
    val points: Int,
    val createdAt: Long,
    val periodKey: String
)

fun recordMissionStudySession(
    context: Context,
    userId: Long,
    durationMinutes: Int,
    subject: String,
    timestamp: Long = System.currentTimeMillis()
) {
    if (userId <= 0 || durationMinutes < MIN_VALID_SESSION_MINUTES) return
    val prefs = context.getSharedPreferences(MISSION_PREFS, Context.MODE_PRIVATE)
    val key = "local_sessions_$userId"
    val current = JSONArray(prefs.getString(key, "[]"))
    current.put(
        JSONObject()
            .put("id", "local_$timestamp")
            .put("date", dateKey(timestamp))
            .put("timestamp", timestamp)
            .put("durationMinutes", durationMinutes)
            .put("subject", subject.ifBlank { "Sem matéria" })
    )
    prefs.edit().putString(key, current.toString()).apply()
}

fun createCustomMissionGoal(
    context: Context,
    userId: Long,
    title: String,
    description: String,
    period: String,
    targetValue: Int
) {
    val cleanTitle = title.trim()
    if (userId <= 0 || cleanTitle.isBlank()) return
    val prefs = context.getSharedPreferences(MISSION_PREFS, Context.MODE_PRIVATE)
    val key = "custom_goals_$userId"
    val current = JSONArray(prefs.getString(key, "[]"))
    val now = System.currentTimeMillis()
    current.put(
        JSONObject()
            .put("id", now)
            .put("title", cleanTitle)
            .put("description", description.trim())
            .put("period", period)
            .put("targetValue", targetValue.coerceAtLeast(1))
            .put("createdAt", now)
    )
    prefs.edit().putString(key, current.toString()).apply()
}

fun buildMissionGamificationState(
    context: Context,
    userId: Long,
    remoteSessions: List<StudySessionResponse>
): MissionGamificationState {
    val events = validStudyEvents(context, userId, remoteSessions)
    val history = loadPointHistory(context, userId).toMutableList()
    val today = todayKey()
    val week = weekKey(System.currentTimeMillis())
    val month = monthKey(System.currentTimeMillis())

    val todayEvents = events.filter { it.date == today }
    val weekEvents = events.filter { weekKey(it.timestamp) == week }
    val monthDays = events.filter { monthKey(it.timestamp) == month }.map { it.date }.distinct()
    val streak = currentStudyStreak(events)
    val weeklyStreak = currentWeeklyStreak(events)

    claimIfNeeded(history, userId, "OFFICIAL", "daily_study", today, 10, todayEvents.size >= 1)
    claimIfNeeded(history, userId, "OFFICIAL", "weekly_study", week, 50, weekEvents.size >= 5)
    claimIfNeeded(history, userId, "OFFICIAL", "seven_day_focus", sevenDayPeriodKey(events), 100, streak >= 7)
    claimIfNeeded(history, userId, "OFFICIAL", "weekly_consistency", week, 150, weeklyStreak >= 3)
    claimIfNeeded(history, userId, "OFFICIAL", "disciplined_month", month, 300, monthDays.size >= 20)

    val customGoals = loadCustomGoals(context, userId)
    val customGoalUi = customGoals.map { goal ->
        val goalEvents = eventsForPeriod(events, goal.period)
        val progress = goalEvents.size
        val completed = progress >= goal.targetValue
        CustomGoalUi(
            id = goal.id,
            title = goal.title,
            description = goal.description,
            period = goal.period.toPeriodLabel(),
            progressText = "${progress.coerceAtMost(goal.targetValue)}/${goal.targetValue} sessões",
            progress = progress.toFloat() / goal.targetValue.toFloat(),
            completed = completed
        )
    }

    savePointHistory(context, userId, history)
    val officialPoints = history.filter { it.sourceType == "OFFICIAL" }.sumOf { it.points }
    val pointsToday = history.filter { it.sourceType == "OFFICIAL" && dateKey(it.createdAt) == today }.sumOf { it.points }

    return MissionGamificationState(
        missions = listOf(
            AppMissionUi("daily_study", "Estudar hoje", "Complete pelo menos 1 sessão hoje.", "${todayEvents.size.coerceAtMost(1)}/1 sessão", todayEvents.size.toFloat(), 10, missionStatus(history, "daily_study", today, todayEvents.size >= 1)),
            AppMissionUi("weekly_study", "Estudar na semana", "Complete 5 sessões válidas na semana.", "${weekEvents.size.coerceAtMost(5)}/5 sessões", weekEvents.size / 5f, 50, missionStatus(history, "weekly_study", week, weekEvents.size >= 5)),
            AppMissionUi("seven_day_focus", "7 dias de foco", "Estude 1 sessão por dia por 7 dias seguidos.", "${streak.coerceAtMost(7)}/7 dias", streak / 7f, 100, missionStatus(history, "seven_day_focus", sevenDayPeriodKey(events), streak >= 7)),
            AppMissionUi("weekly_consistency", "Constância semanal", "Cumpra a meta semanal por 3 semanas seguidas.", "${weeklyStreak.coerceAtMost(3)}/3 semanas", weeklyStreak / 3f, 150, missionStatus(history, "weekly_consistency", week, weeklyStreak >= 3)),
            AppMissionUi("disciplined_month", "Mês disciplinado", "Estude em pelo menos 20 dias no mês.", "${monthDays.size.coerceAtMost(20)}/20 dias", monthDays.size / 20f, 300, missionStatus(history, "disciplined_month", month, monthDays.size >= 20))
        ),
        customGoals = customGoalUi,
        points = UserPointsUi(
            officialPoints = officialPoints,
            totalPoints = officialPoints,
            pointsToday = pointsToday,
            currentStudyStreak = streak,
            weeklyStreak = weeklyStreak
        )
    )
}

private fun claimIfNeeded(history: MutableList<PointHistoryEntry>, userId: Long, sourceType: String, sourceId: String, periodKey: String, points: Int, completed: Boolean) {
    if (!completed || hasHistory(history, sourceType, sourceId, periodKey)) return
    history.add(PointHistoryEntry(System.currentTimeMillis(), userId, sourceType, sourceId, points, System.currentTimeMillis(), periodKey))
}

private fun missionStatus(history: List<PointHistoryEntry>, sourceId: String, periodKey: String, completed: Boolean): String =
    when {
        hasHistory(history, "OFFICIAL", sourceId, periodKey) -> "Resgatada"
        completed -> "Concluída"
        else -> "Em andamento"
    }

private fun hasHistory(history: List<PointHistoryEntry>, sourceType: String, sourceId: String, periodKey: String): Boolean =
    history.any { it.sourceType == sourceType && it.sourceId == sourceId && it.periodKey == periodKey }

private fun validStudyEvents(context: Context, userId: Long, remoteSessions: List<StudySessionResponse>): List<MissionStudyEvent> {
    val remote = remoteSessions.mapNotNull { session ->
        if (session.durationMinutes < MIN_VALID_SESSION_MINUTES) return@mapNotNull null
        val timestamp = parseSessionTimestamp(session.endedAt ?: session.startedAt) ?: System.currentTimeMillis()
        MissionStudyEvent("remote_${session.id}", dateKey(timestamp), timestamp, session.durationMinutes, session.subject)
    }
    val local = loadLocalSessions(context, userId)
    return (remote + local)
        .distinctBy { it.id }
        .sortedBy { it.timestamp }
}

private fun loadLocalSessions(context: Context, userId: Long): List<MissionStudyEvent> {
    val raw = context.getSharedPreferences(MISSION_PREFS, Context.MODE_PRIVATE).getString("local_sessions_$userId", "[]") ?: "[]"
    val array = JSONArray(raw)
    return List(array.length()) { index ->
        val item = array.getJSONObject(index)
        MissionStudyEvent(
            id = item.optString("id"),
            date = item.optString("date"),
            timestamp = item.optLong("timestamp"),
            durationMinutes = item.optInt("durationMinutes"),
            subject = item.optString("subject")
        )
    }.filter { it.durationMinutes >= MIN_VALID_SESSION_MINUTES }
}

private fun loadCustomGoals(context: Context, userId: Long): List<StoredCustomGoal> {
    val raw = context.getSharedPreferences(MISSION_PREFS, Context.MODE_PRIVATE).getString("custom_goals_$userId", "[]") ?: "[]"
    val array = JSONArray(raw)
    return List(array.length()) { index ->
        val item = array.getJSONObject(index)
        StoredCustomGoal(
            id = item.optLong("id"),
            title = item.optString("title"),
            description = item.optString("description"),
            period = item.optString("period", "WEEK"),
            targetValue = item.optInt("targetValue", 1),
            createdAt = item.optLong("createdAt")
        )
    }
}

private fun loadPointHistory(context: Context, userId: Long): List<PointHistoryEntry> {
    val raw = context.getSharedPreferences(MISSION_PREFS, Context.MODE_PRIVATE).getString("point_history_$userId", "[]") ?: "[]"
    val array = JSONArray(raw)
    return List(array.length()) { index ->
        val item = array.getJSONObject(index)
        PointHistoryEntry(
            id = item.optLong("id"),
            userId = item.optLong("userId"),
            sourceType = item.optString("sourceType"),
            sourceId = item.optString("sourceId"),
            points = item.optInt("points"),
            createdAt = item.optLong("createdAt"),
            periodKey = item.optString("periodKey")
        )
    }
}

private fun savePointHistory(context: Context, userId: Long, history: List<PointHistoryEntry>) {
    val array = JSONArray()
    history.forEach {
        array.put(
            JSONObject()
                .put("id", it.id)
                .put("userId", it.userId)
                .put("sourceType", it.sourceType)
                .put("sourceId", it.sourceId)
                .put("points", it.points)
                .put("createdAt", it.createdAt)
                .put("periodKey", it.periodKey)
        )
    }
    context.getSharedPreferences(MISSION_PREFS, Context.MODE_PRIVATE).edit().putString("point_history_$userId", array.toString()).apply()
}

private fun eventsForPeriod(events: List<MissionStudyEvent>, period: String): List<MissionStudyEvent> {
    val now = System.currentTimeMillis()
    return when (period) {
        "DAY" -> events.filter { it.date == todayKey() }
        "MONTH" -> events.filter { monthKey(it.timestamp) == monthKey(now) }
        "ALL" -> events
        else -> events.filter { weekKey(it.timestamp) == weekKey(now) }
    }
}

private fun periodKeyForGoal(period: String): String {
    val now = System.currentTimeMillis()
    return when (period) {
        "DAY" -> todayKey()
        "MONTH" -> monthKey(now)
        "ALL" -> "all_time"
        else -> weekKey(now)
    }
}

private fun currentStudyStreak(events: List<MissionStudyEvent>): Int {
    val studiedDays = events.map { it.date }.toSet()
    val cal = Calendar.getInstance()
    if (!studiedDays.contains(todayKey())) cal.add(Calendar.DAY_OF_YEAR, -1)
    var streak = 0
    while (studiedDays.contains(dateKey(cal.timeInMillis))) {
        streak += 1
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return streak
}

private fun currentWeeklyStreak(events: List<MissionStudyEvent>): Int {
    val cal = Calendar.getInstance()
    var streak = 0
    while (true) {
        val key = weekKey(cal.timeInMillis)
        val completed = events.count { weekKey(it.timestamp) == key } >= 5
        if (!completed) return streak
        streak += 1
        cal.add(Calendar.WEEK_OF_YEAR, -1)
    }
}

private fun sevenDayPeriodKey(events: List<MissionStudyEvent>): String {
    val days = events.map { it.date }.distinct().sorted()
    return if (days.size >= 7) days.takeLast(7).joinToString("_") else "incomplete_${todayKey()}"
}

private fun parseSessionTimestamp(raw: String?): Long? {
    if (raw.isNullOrBlank()) return null
    val normalized = raw.replace("T", " ").take(19)
    val formats = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd")
    return formats.firstNotNullOfOrNull { format ->
        runCatching { SimpleDateFormat(format, Locale.getDefault()).parse(normalized)?.time }.getOrNull()
    }
}

private fun todayKey(): String = dateKey(System.currentTimeMillis())

private fun dateKey(timestamp: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp)

private fun monthKey(timestamp: Long): String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(timestamp)

private fun weekKey(timestamp: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val year = cal.get(Calendar.YEAR)
    val week = cal.get(Calendar.WEEK_OF_YEAR).toString().padStart(2, '0')
    return "$year-W$week"
}

private fun String.toPeriodLabel(): String = when (this) {
    "DAY" -> "Hoje"
    "MONTH" -> "Mês"
    "ALL" -> "Sempre"
    else -> "Semana"
}
