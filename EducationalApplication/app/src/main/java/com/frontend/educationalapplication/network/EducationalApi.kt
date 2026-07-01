package com.frontend.educationalapplication.network

import com.frontend.educationalapplication.BuildConfig
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
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

    suspend fun updateUserName(userId: Long, name: String): AuthResponse = post(
        path = "/api/users/$userId/name",
        body = JSONObject().put("name", name)
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
        focusRating: Int,
        exerciseCount: Int? = null,
        correctAnswers: Int? = null
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
            .put("exerciseCount", exerciseCount)
            .put("correctAnswers", correctAnswers)
    ).toStudySessionResponse()

    suspend fun studySessions(userId: Long): List<StudySessionResponse> =
        getArray("/api/study-sessions/user/$userId").map { it.toStudySessionResponse() }

    suspend fun deleteStudySession(sessionId: Long) {
        requestAllowEmpty("DELETE", "/api/study-sessions/$sessionId")
    }

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

    suspend fun createReview(
        userId: Long,
        title: String,
        subject: String,
        dueDate: String
    ): ReviewReminderResponse = post(
        path = "/api/reviews",
        body = JSONObject()
            .put("userId", userId)
            .put("title", title)
            .put("subject", subject)
            .put("dueDate", dueDate)
    ).toReviewReminderResponse()

    suspend fun completeReview(reviewId: Long): ReviewReminderResponse =
        post("/api/reviews/$reviewId/complete", JSONObject()).toReviewReminderResponse()

    suspend fun deleteReview(reviewId: Long) {
        requestAllowEmpty("DELETE", "/api/reviews/$reviewId")
    }

    suspend fun rewardCoins(userId: Long, amount: Int, reason: String): Int =
        post(
            path = "/api/coins/user/$userId/reward",
            body = JSONObject()
                .put("amount", amount)
                .put("reason", reason)
        ).getInt("coins")

    suspend fun calendar(userId: Long): List<CalendarDayResponse> =
        getArray("/api/calendar/user/$userId").map { it.toCalendarDayResponse() }

    suspend fun rankingWeekly(currentUserId: Long): RankingResponse =
        getObject("/api/ranking/weekly?currentUserId=$currentUserId").toRankingResponse()

    suspend fun rankingAllTime(currentUserId: Long): RankingResponse =
        getObject("/api/ranking/all-time?currentUserId=$currentUserId").toRankingResponse()

    suspend fun generateQuizFromText(
        text: String,
        subject: String,
        gameMode: String,
        questionCount: Int
    ): GeneratedQuizResponse = post(
        path = "/api/ai/quiz-from-text",
        body = JSONObject()
            .put("text", text)
            .put("subject", subject)
            .put("gameMode", gameMode)
            .put("questionCount", questionCount)
    ).toGeneratedQuizResponse()

    suspend fun saveGeneratedQuestions(
        subject: String,
        gameMode: String,
        questions: List<GeneratedQuestionResponse>
    ): List<SavedQuestionResponse> = postArrayResponse(
        path = "/api/ai/questions",
        body = JSONObject()
            .put("subject", subject)
            .put("gameMode", gameMode)
            .put("questions", JSONArray(questions.map { it.toJson() }))
    ).map { it.toSavedQuestionResponse() }

    suspend fun savedQuestions(query: String = ""): List<SavedQuestionResponse> {
        val suffix = if (query.isBlank()) "" else "?query=${java.net.URLEncoder.encode(query, "UTF-8")}"
        return getArray("/api/ai/questions$suffix").map { it.toSavedQuestionResponse() }
    }

    suspend fun updateSavedQuestion(
        id: Long,
        subject: String
    ): SavedQuestionResponse = request(
        method = "PUT",
        path = "/api/ai/questions/$id",
        body = JSONObject()
            .put("subject", subject)
            .put("gameMode", "Universal")
    ).toSavedQuestionResponse()

    suspend fun deleteSavedQuestion(id: Long) {
        requestAllowEmpty("DELETE", "/api/ai/questions/$id")
    }

    suspend fun questionLists(): List<QuestionListResponse> =
        getArray("/api/ai/question-lists").map { it.toQuestionListResponse() }

    suspend fun createQuestionList(name: String): QuestionListResponse =
        post("/api/ai/question-lists", JSONObject().put("name", name)).toQuestionListResponse()

    suspend fun renameQuestionList(id: Long, name: String): QuestionListResponse =
        request("PUT", "/api/ai/question-lists/$id", JSONObject().put("name", name)).toQuestionListResponse()

    suspend fun deleteQuestionList(id: Long) {
        requestAllowEmpty("DELETE", "/api/ai/question-lists/$id")
    }

    suspend fun addQuestionToList(listId: Long, questionId: Long): QuestionListResponse =
        post("/api/ai/question-lists/$listId/questions/$questionId", JSONObject()).toQuestionListResponse()

    suspend fun removeQuestionFromList(listId: Long, questionId: Long): QuestionListResponse =
        request("DELETE", "/api/ai/question-lists/$listId/questions/$questionId").toQuestionListResponse()

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

    private suspend fun postArrayResponse(path: String, body: JSONObject): List<JSONObject> =
        withContext(Dispatchers.IO) {
            val connection = openConnection("POST", path)
            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer -> writer.write(body.toString()) }
            val response = readResponse(connection)
            JSONArray(response).let { array -> List(array.length()) { index -> array.getJSONObject(index) } }
        }

    private suspend fun request(method: String, path: String, body: JSONObject? = null): JSONObject =
        withContext(Dispatchers.IO) {
            // Generic request for endpoints that return one JSON object.
            val connection = openConnection(method, path)
            if (body != null) {
                OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(body.toString())
                }
            }
            JSONObject(readResponse(connection))
        }

    private suspend fun requestAllowEmpty(method: String, path: String, body: JSONObject? = null): String =
        withContext(Dispatchers.IO) {
            val connection = openConnection(method, path)
            if (body != null) {
                OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer -> writer.write(body.toString()) }
            }
            readResponse(connection)
        }

    private fun openConnection(method: String, path: String): HttpURLConnection {
        // HttpURLConnection is built into Java/Android. Later we can replace this with Retrofit.
        return (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10000
            readTimeout = 60000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            if (method == "POST" || method == "PUT") {
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
        val response = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
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
    startedAt = optString("startedAt").takeIf { it.isNotBlank() },
    endedAt = optString("endedAt").takeIf { it.isNotBlank() },
    studyType = optString("studyType").takeIf { it.isNotBlank() },
    content = optString("content").takeIf { it.isNotBlank() },
    objective = optString("objective").takeIf { it.isNotBlank() },
    notes = optString("notes").takeIf { it.isNotBlank() },
    learned = optString("learned").takeIf { it.isNotBlank() },
    difficulties = optString("difficulties").takeIf { it.isNotBlank() },
    nextAction = optString("nextAction").takeIf { it.isNotBlank() },
    focusRating = if (has("focusRating") && !isNull("focusRating")) getInt("focusRating") else null,
    exerciseCount = if (has("exerciseCount") && !isNull("exerciseCount")) getInt("exerciseCount") else null,
    correctAnswers = if (has("correctAnswers") && !isNull("correctAnswers")) getInt("correctAnswers") else null
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

private fun JSONObject.toGeneratedQuizResponse(): GeneratedQuizResponse {
    val questionsArray = getJSONArray("questions")
    return GeneratedQuizResponse(
        source = optString("source"),
        gameMode = optString("gameMode"),
        questions = List(questionsArray.length()) { index ->
            questionsArray.getJSONObject(index).toGeneratedQuestionResponse()
        }
    )
}

private fun JSONObject.toGeneratedQuestionResponse(): GeneratedQuestionResponse {
    val optionsArray = getJSONArray("options")
    return GeneratedQuestionResponse(
        question = optString("question"),
        options = List(optionsArray.length()) { index -> optionsArray.getString(index) },
        correctIndex = optInt("correctIndex"),
        explanation = optString("explanation")
    )
}

private fun GeneratedQuestionResponse.toJson(): JSONObject =
    JSONObject()
        .put("question", question)
        .put("options", JSONArray(options))
        .put("correctIndex", correctIndex)
        .put("explanation", explanation)

private fun JSONObject.toSavedQuestionResponse(): SavedQuestionResponse {
    val optionsArray = getJSONArray("options")
    return SavedQuestionResponse(
        id = optLong("id"),
        subject = optString("subject"),
        gameMode = optString("gameMode"),
        question = optString("question"),
        options = List(optionsArray.length()) { index -> optionsArray.getString(index) },
        correctIndex = optInt("correctIndex"),
        explanation = optString("explanation")
    )
}

private fun JSONObject.toQuestionListResponse(): QuestionListResponse {
    val idsArray = optJSONArray("questionIds") ?: JSONArray()
    return QuestionListResponse(
        id = optLong("id"),
        name = optString("name"),
        questionCount = optLong("questionCount"),
        questionIds = List(idsArray.length()) { index -> idsArray.getLong(index) }
    )
}

private fun JSONObject.toRankingResponse(): RankingResponse {
    val playersArray = getJSONArray("players")
    return RankingResponse(
        period = optString("period"),
        players = List(playersArray.length()) { index ->
            playersArray.getJSONObject(index).toRankingPlayerResponse()
        }
    )
}

private fun JSONObject.toRankingPlayerResponse(): RankingPlayerResponse =
    RankingPlayerResponse(
        userId = optLong("userId"),
        name = optString("name"),
        minutes = optInt("minutes"),
        position = optInt("position"),
        currentUser = optBoolean("currentUser")
    )
