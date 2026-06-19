package com.frontend.educationalapplication.network

import com.frontend.educationalapplication.BuildConfig
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class EducationalApi(
    // 10.0.2.2 is the Android emulator's special address for your computer's localhost.
    // Backend on PC: http://localhost:8080
    // Backend from emulator: http://10.0.2.2:8080
    private val baseUrl: String = BuildConfig.API_BASE_URL
) {
    // Public functions below are what the UI calls. Each one maps to a Spring endpoint.
    suspend fun login(email: String, password: String): AuthResponse = post(
        path = "/api/auth/login",
        body = JSONObject()
            .put("email", email)
            .put("password", password)
    ).toAuthResponse()

    suspend fun register(name: String, email: String, password: String): AuthResponse = post(
        path = "/api/auth/register",
        body = JSONObject()
            .put("name", name)
            .put("email", email)
            .put("password", password)
    ).toAuthResponse()

    suspend fun startPomodoro(
        userId: Long,
        subject: String,
        focusMinutes: Int,
        breakMinutes: Int
    ): PomodoroResponse = post(
        path = "/api/pomodoros",
        body = JSONObject()
            .put("userId", userId)
            .put("subject", subject)
            .put("focusMinutes", focusMinutes)
            .put("breakMinutes", breakMinutes)
    ).toPomodoroResponse()

    suspend fun extendPomodoro(id: Long): PomodoroResponse =
        post("/api/pomodoros/$id/extend", JSONObject()).toPomodoroResponse()

    suspend fun completePomodoro(id: Long, notes: String): PomodoroResponse =
        post("/api/pomodoros/$id/complete", JSONObject().put("notes", notes)).toPomodoroResponse()

    suspend fun createManualSession(
        userId: Long,
        subject: String,
        durationMinutes: Int,
        studyType: String,
        content: String,
        objective: String,
        notes: String,
        learned: String,
        difficulties: String,
        nextAction: String,
        focusRating: Int
    ): StudySessionResponse = post(
        path = "/api/study-sessions/manual",
        body = JSONObject()
            .put("userId", userId)
            .put("subject", subject)
            .put("durationMinutes", durationMinutes)
            .put("studyType", studyType)
            .put("content", content)
            .put("objective", objective)
            .put("notes", notes)
            .put("learned", learned)
            .put("difficulties", difficulties)
            .put("nextAction", nextAction)
            .put("focusRating", focusRating)
    ).toStudySessionResponse()

    suspend fun studySessions(userId: Long): List<StudySessionResponse> =
        getArray("/api/study-sessions/user/$userId").map { it.toStudySessionResponse() }

    suspend fun goals(userId: Long): List<GoalResponse> =
        getArray("/api/goals/user/$userId").map { it.toGoalResponse() }

    suspend fun missions(userId: Long): List<MissionResponse> =
        getArray("/api/missions/user/$userId").map { it.toMissionResponse() }

    suspend fun claimMission(missionId: Long): MissionResponse =
        post("/api/missions/$missionId/claim", JSONObject()).toMissionResponse()

    suspend fun progress(userId: Long): ProgressResponse =
        getObject("/api/stats/user/$userId/progress?days=7").toProgressResponse()

    suspend fun reviews(userId: Long): List<ReviewReminderResponse> =
        getArray("/api/reviews/user/$userId").map { it.toReviewReminderResponse() }

    suspend fun completeReview(reviewId: Long): ReviewReminderResponse =
        post("/api/reviews/$reviewId/complete", JSONObject()).toReviewReminderResponse()

    suspend fun calendar(userId: Long): List<CalendarDayResponse> =
        getArray("/api/calendar/user/$userId").map { it.toCalendarDayResponse() }

    private suspend fun getObject(path: String): JSONObject = request("GET", path)

    private suspend fun getArray(path: String): List<JSONObject> = withContext(Dispatchers.IO) {
        // Network work must run on Dispatchers.IO so it does not block the Android UI thread.
        val connection = openConnection("GET", path)
        val response = readResponse(connection)
        JSONArray(response).let { array ->
            List(array.length()) { index -> array.getJSONObject(index) }
        }
    }

    private suspend fun post(path: String, body: JSONObject): JSONObject =
        request("POST", path, body)

    private suspend fun request(method: String, path: String, body: JSONObject? = null): JSONObject =
        withContext(Dispatchers.IO) {
            // Generic request for endpoints that return one JSON object.
            val connection = openConnection(method, path)
            if (body != null) {
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(body.toString())
                }
            }
            JSONObject(readResponse(connection))
        }

    private fun openConnection(method: String, path: String): HttpURLConnection {
        // HttpURLConnection is built into Java/Android. Later we can replace this with Retrofit.
        return (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            if (method == "POST") {
                doOutput = true
            }
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        // For errors, Spring returns JSON like {"error":"message"}, so we try to show that.
        val stream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }
        val response = stream.bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) {
            val message = runCatching { JSONObject(response).optString("error") }.getOrNull()
            throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: response)
        }
        return response
    }
}

// These extension functions convert raw JSON from the backend into Kotlin data classes.
// Example: JSONObject.toAuthResponse() returns AuthResponse(...).
private fun JSONObject.toAuthResponse() = AuthResponse(
    userId = getLong("userId"),
    name = getString("name"),
    email = getString("email"),
    coins = getInt("coins")
)

private fun JSONObject.toPomodoroResponse() = PomodoroResponse(
    id = getLong("id"),
    userId = getLong("userId"),
    subject = getString("subject"),
    focusMinutes = getInt("focusMinutes"),
    breakMinutes = getInt("breakMinutes"),
    extendedMinutes = getInt("extendedMinutes"),
    totalFocusMinutes = getInt("totalFocusMinutes"),
    status = getString("status")
)

private fun JSONObject.toStudySessionResponse() = StudySessionResponse(
    id = getLong("id"),
    subject = getString("subject"),
    durationMinutes = getInt("durationMinutes"),
    source = getString("source"),
    studyType = optString("studyType").takeIf { it.isNotBlank() },
    content = optString("content").takeIf { it.isNotBlank() },
    objective = optString("objective").takeIf { it.isNotBlank() },
    notes = optString("notes").takeIf { it.isNotBlank() },
    learned = optString("learned").takeIf { it.isNotBlank() },
    difficulties = optString("difficulties").takeIf { it.isNotBlank() },
    nextAction = optString("nextAction").takeIf { it.isNotBlank() },
    focusRating = if (has("focusRating") && !isNull("focusRating")) getInt("focusRating") else null
)

private fun JSONObject.toGoalResponse() = GoalResponse(
    id = getLong("id"),
    title = getString("title"),
    subject = getString("subject"),
    targetMinutes = getInt("targetMinutes"),
    currentMinutes = getInt("currentMinutes"),
    progressPercent = getDouble("progressPercent"),
    dueDate = getString("dueDate"),
    daysUntilDueDate = getLong("daysUntilDueDate"),
    completed = getBoolean("completed")
)

private fun JSONObject.toMissionResponse() = MissionResponse(
    id = getLong("id"),
    title = getString("title"),
    description = getString("description"),
    targetMinutes = getInt("targetMinutes"),
    progressMinutes = getInt("progressMinutes"),
    rewardCoins = getInt("rewardCoins"),
    completed = getBoolean("completed"),
    claimed = getBoolean("claimed")
)

private fun JSONObject.toProgressResponse(): ProgressResponse {
    val days = getJSONArray("byDay")
    val subjects = getJSONArray("bySubject")
    return ProgressResponse(
        userId = getLong("userId"),
        totalMinutes = getInt("totalMinutes"),
        byDay = List(days.length()) { index ->
            days.getJSONObject(index).let {
                DailyStudyMinutes(it.getString("date"), it.getInt("minutes"))
            }
        },
        bySubject = List(subjects.length()) { index ->
            subjects.getJSONObject(index).let {
                SubjectStudyMinutes(it.getString("subject"), it.getInt("minutes"))
            }
        }
    )
}

private fun JSONObject.toReviewReminderResponse() = ReviewReminderResponse(
    id = getLong("id"),
    title = getString("title"),
    subject = getString("subject"),
    dueDate = getString("dueDate"),
    completed = getBoolean("completed")
)

private fun JSONObject.toCalendarDayResponse(): CalendarDayResponse {
    val subjectArray = getJSONArray("subjects")
    return CalendarDayResponse(
        date = getString("date"),
        totalMinutes = getInt("totalMinutes"),
        subjects = List(subjectArray.length()) { index ->
            subjectArray.getJSONObject(index).let {
                CalendarSubjectMinutes(
                    subject = it.getString("subject"),
                    minutes = it.getInt("minutes")
                )
            }
        }
    )
}
