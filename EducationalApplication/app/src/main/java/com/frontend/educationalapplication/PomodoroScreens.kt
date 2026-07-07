package com.frontend.educationalapplication

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frontend.educationalapplication.network.AuthResponse
import com.frontend.educationalapplication.network.CalendarDayResponse
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.GoalResponse
import com.frontend.educationalapplication.network.MissionResponse
import com.frontend.educationalapplication.network.ProgressResponse
import com.frontend.educationalapplication.network.ReviewReminderResponse
import com.frontend.educationalapplication.network.StudySessionResponse
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import com.frontend.educationalapplication.ui.theme.LearnerTeal
import com.frontend.educationalapplication.ui.theme.LearnerYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

class PomodoroPlayerState {
    var subject by mutableStateOf("Matem\u00e1tica")
    var content by mutableStateOf("")
    var studyType by mutableStateOf("Revis\u00e3o")
    var focusMinutes by mutableStateOf("25")
    var breakMinutes by mutableStateOf("5")
    var pomodoroId by mutableLongStateOf(0L)
    var remainingSeconds by mutableIntStateOf(0)
    var totalSeconds by mutableIntStateOf(0)
    var running by mutableStateOf(false)
    var minimized by mutableStateOf(false)
    var showQuickReport by mutableStateOf(false)
    var perceivedDifficulty by mutableStateOf("Ok")
    var quickNote by mutableStateOf("")
    var scheduleReview by mutableStateOf(true)
    var activeSubjectId by mutableLongStateOf(0L)
    var activeTopicId by mutableLongStateOf(0L)
    var activeTaskId by mutableLongStateOf(0L)
    var activeTaskTitle by mutableStateOf("")
    var status by mutableStateOf("Pronto")
    var timerAccent by mutableStateOf(LearnerPrimary)

    val hasActiveTimer: Boolean
        get() = totalSeconds > 0 && !showQuickReport
}

@Composable
fun rememberPomodoroPlayerState(): PomodoroPlayerState = remember { PomodoroPlayerState() }

@Composable
fun PomodoroTicker(state: PomodoroPlayerState) {
    LaunchedEffect(state.running) {
        while (state.running && state.remainingSeconds > 0) {
            delay(1.seconds)
            state.remainingSeconds -= 1
        }
        if (state.running && state.remainingSeconds == 0) {
            state.running = false
            state.minimized = false
            state.showQuickReport = true
            state.status = "Foco conclu\u00eddo"
        }
    }
}

@Composable
fun PomodoroDetailScreen(
    api: EducationalApi,
    userId: Long,
    playerState: PomodoroPlayerState,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onMinimize: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReportSubscreenHeader(
            title = "Pomodoro",
            onBack = onBack
        )
        PomodoroScreen(api = api, userId = userId, playerState = playerState, onDone = onDone, onMinimize = onMinimize)
        BottomBackButton(onBack = onBack)
    }
}

@Composable
fun PomodoroScreen(
    api: EducationalApi,
    userId: Long,
    playerState: PomodoroPlayerState,
    onDone: () -> Unit,
    onMinimize: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val plannedSubjects = remember(userId) { loadStudyPlanningSubjects(context, userId) }
    val selectedPlan = plannedSubjects.firstOrNull { it.name == playerState.subject }
    val topics = selectedPlan?.topics.orEmpty()

    LaunchedEffect(userId) {
        loadPomodoroPrefill(context)?.let { prefill ->
            playerState.activeSubjectId = prefill.subjectId
            playerState.activeTopicId = prefill.topicId
            playerState.activeTaskId = prefill.taskId
            playerState.activeTaskTitle = prefill.taskTitle
            playerState.subject = prefill.subject
            playerState.content = prefill.topic
            playerState.studyType = prefill.studyType
            clearPomodoroPrefill(context)
            playerState.status = "Pronto para estudar: ${prefill.taskTitle.ifBlank { prefill.topic.ifBlank { prefill.subject } }}"
        }
    }

    LaunchedEffect(userId) {
        if (userId > 0) syncPendingPomodoros(context, api, userId) { synced ->
            if (synced > 0) {
                playerState.status = "$synced sess\u00e3o offline sincronizada"
                onDone()
            }
        }
    }

    SectionCard("Pomodoro") {
        PomodoroTimerCircle(
            timerText(playerState.remainingSeconds),
            if (playerState.running) "FOCO" else playerState.status.uppercase(),
            playerState.timerAccent,
            if (playerState.totalSeconds > 0) playerState.remainingSeconds.toFloat() / playerState.totalSeconds else 0f
        )
        if (playerState.hasActiveTimer) {
            TextButton(
                onClick = {
                    playerState.minimized = true
                    onMinimize()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Minimizar") }
        }
        PomodoroColorSelector(selected = playerState.timerAccent, onSelected = { playerState.timerAccent = it })
        Spacer(Modifier.height(12.dp))
        if (plannedSubjects.isNotEmpty()) {
            Text("Mat\u00e9ria do Planejamento (opcional)", color = LearnerMutedText, fontWeight = FontWeight.Bold)
            plannedSubjects.forEach { plan ->
                PomodoroPlanChip(plan.name, playerState.subject == plan.name) {
                    playerState.subject = plan.name
                    playerState.content = ""
                }
            }
            if (topics.isNotEmpty()) {
                Text("T\u00f3pico/conte\u00fado (opcional)", color = LearnerMutedText, fontWeight = FontWeight.Bold)
                topics.forEach { topic -> PomodoroPlanChip(topic.name, playerState.content == topic.name) { playerState.content = topic.name } }
            }
        }
        OutlinedTextField(value = playerState.subject, onValueChange = { playerState.subject = it }, label = { Text("Mat\u00e9ria") })
        OutlinedTextField(value = playerState.content, onValueChange = { playerState.content = it }, label = { Text("T\u00f3pico/conte\u00fado opcional") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Leitura", "Exerc\u00edcio", "Revis\u00e3o").forEach { type ->
                PomodoroPlanChip(type, playerState.studyType == type, Modifier.weight(1f)) { playerState.studyType = type }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField("Foco", playerState.focusMinutes, { playerState.focusMinutes = it }, Modifier.weight(1f))
            NumberField("Pausa", playerState.breakMinutes, { playerState.breakMinutes = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                val focus = playerState.focusMinutes.toIntOrNull() ?: 25
                val pause = playerState.breakMinutes.toIntOrNull() ?: 5
                playerState.pomodoroId = 0L
                playerState.totalSeconds = focus * 60
                playerState.remainingSeconds = playerState.totalSeconds
                playerState.running = true
                playerState.minimized = false
                playerState.showQuickReport = false
                playerState.status = "Em andamento"
                if (userId > 0) {
                    scope.launch {
                        runCatching { api.startPomodoro(userId, playerState.subject.ifBlank { "Pomodoro" }, focus, pause) }
                            .onSuccess { playerState.pomodoroId = it.id; playerState.status = "Sincronizado" }
                            .onFailure { playerState.status = "Offline - timer local" }
                    }
                } else {
                    playerState.status = "Modo convidado - timer local"
                }
            }, modifier = Modifier.weight(1f)) { Text("Come\u00e7ar") }
            Button(onClick = { if (playerState.totalSeconds > 0) playerState.running = !playerState.running }, modifier = Modifier.weight(1f)) {
                Text(if (playerState.running) "Pausar" else "Continuar")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                if (playerState.totalSeconds == 0) return@Button
                playerState.totalSeconds += 5 * 60
                playerState.remainingSeconds += 5 * 60
                playerState.status = "+5 minutos"
                if (playerState.pomodoroId != 0L) scope.launch {
                    runCatching { api.extendPomodoro(playerState.pomodoroId) }
                        .onFailure { playerState.status = "+5 local - sincroniza\u00e7\u00e3o pendente" }
                }
            }, modifier = Modifier.weight(1f)) { Text("+5") }
            Button(onClick = {
                if (playerState.totalSeconds != 0) {
                    playerState.running = false
                    playerState.minimized = false
                    playerState.showQuickReport = true
                    playerState.status = "Sess\u00e3o conclu\u00edda"
                }
            }, modifier = Modifier.weight(1f)) { Text("Concluir") }
        }
        if (playerState.showQuickReport) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sess\u00e3o conclu\u00edda", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text(
                        listOf(
                            playerState.subject.takeIf { it.isNotBlank() }?.let { "Mat\u00e9ria: $it" },
                            playerState.content.takeIf { it.isNotBlank() }?.let { "T\u00f3pico: $it" },
                            playerState.activeTaskTitle.takeIf { it.isNotBlank() }?.let { "Tarefa: $it" },
                            "Tipo: ${playerState.studyType}",
                            "Tempo: ${(playerState.totalSeconds / 60).coerceAtLeast(1)} min"
                        ).filterNotNull().joinToString(" \u2022 "),
                        color = LearnerMutedText,
                        fontSize = 13.sp
                    )
                    Text("Como foi essa sess\u00e3o?", color = LearnerMutedText)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Dif\u00edcil", "Ok", "F\u00e1cil").forEach { option ->
                            PomodoroPlanChip(option, playerState.perceivedDifficulty == option, Modifier.weight(1f)) { playerState.perceivedDifficulty = option }
                        }
                    }
                    PomodoroPlanChip(
                        if (playerState.scheduleReview) "Agendar revis\u00e3o autom\u00e1tica" else "+ Agendar revis\u00e3o?",
                        playerState.scheduleReview,
                        Modifier.fillMaxWidth()
                    ) { playerState.scheduleReview = !playerState.scheduleReview }
                    OutlinedTextField(
                        value = playerState.quickNote,
                        onValueChange = { playerState.quickNote = it },
                        label = { Text("Anota\u00e7\u00e3o r\u00e1pida (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    fun saveQuickReport(completeTask: Boolean) {
                        val completedMinutes = (playerState.totalSeconds / 60).coerceAtLeast(1)
                        val notes = listOf(
                            "Dificuldade percebida: ${playerState.perceivedDifficulty}",
                            playerState.activeTaskTitle.takeIf { it.isNotBlank() }?.let { "Tarefa: $it" },
                            playerState.content.takeIf { it.isNotBlank() }?.let { "T\u00f3pico: $it" },
                            "Tipo: ${playerState.studyType}",
                            playerState.quickNote.takeIf { it.isNotBlank() }
                        ).filterNotNull().joinToString(" | ")
                        fun finishLocal(recordForMissions: Boolean = false) {
                            if (completeTask) markStudyTaskCompleted(context, userId, playerState.activeSubjectId, playerState.activeTopicId, playerState.activeTaskId)
                            if (recordForMissions) recordMissionStudySession(context, userId, completedMinutes, playerState.subject)
                            playerState.showQuickReport = false
                            playerState.minimized = false
                            playerState.quickNote = ""
                        }
                        fun createReview() {
                            if (playerState.scheduleReview && userId > 0) scope.launch {
                                runCatching {
                                    api.createReview(
                                        userId,
                                        "Revisar ${playerState.content.ifBlank { playerState.subject.ifBlank { "sess\u00e3o de estudo" } }}",
                                        playerState.subject.ifBlank { "Sem mat\u00e9ria" },
                                        pomodoroReviewDateAfter(reviewDaysForDifficulty(playerState.perceivedDifficulty)),
                                        topic = playerState.content,
                                        taskTitle = playerState.activeTaskTitle,
                                        notes = notes,
                                        originalDifficulty = playerState.perceivedDifficulty.toDifficultyCode()
                                    )
                                }
                            }
                        }
                        if (playerState.pomodoroId != 0L) {
                            scope.launch {
                                runCatching { api.completePomodoro(playerState.pomodoroId, notes) }
                                    .onSuccess { finishLocal(); createReview(); playerState.status = "Salvo"; onDone() }
                                    .onFailure {
                                        savePendingPomodoro(context, playerState.subject, completedMinutes, playerState.content, playerState.studyType)
                                        finishLocal(recordForMissions = true)
                                        playerState.pomodoroId = 0L
                                        playerState.status = "Salvo local - sincroniza depois"
                                    }
                            }
                        } else {
                            savePendingPomodoro(context, playerState.subject, completedMinutes, playerState.content, playerState.studyType)
                            finishLocal(recordForMissions = true)
                            playerState.status = "Salvo local - sincroniza depois"
                        }
                    }
                    Button(onClick = { saveQuickReport(false) }, modifier = Modifier.fillMaxWidth()) { Text("Salvar") }
                    if (playerState.activeTaskId != 0L) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { saveQuickReport(true) }, modifier = Modifier.weight(1f)) { Text("Salvar e concluir tarefa") }
                        TextButton(onClick = { saveQuickReport(false) }, modifier = Modifier.weight(1f)) { Text("Salvar sem concluir") }
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroPictureInPictureContent(state: PomodoroPlayerState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(state.timerAccent, state.timerAccent.copy(alpha = 0.78f))
                )
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timerText(state.remainingSeconds),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Foco",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    maxLines = 1
                )
                Text(
                    text = listOf(state.subject, state.content)
                        .filter { it.isNotBlank() }
                        .joinToString(" / ")
                        .ifBlank { "Pomodoro" },
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

data class PomodoroPrefill(
    val subjectId: Long = 0L,
    val topicId: Long = 0L,
    val taskId: Long = 0L,
    val subject: String,
    val topic: String,
    val studyType: String,
    val taskTitle: String
)

fun savePomodoroPrefill(context: Context, prefill: PomodoroPrefill) {
    context.getSharedPreferences("pomodoro_prefill", Context.MODE_PRIVATE).edit().putString("next", JSONObject().put("subjectId", prefill.subjectId).put("topicId", prefill.topicId).put("taskId", prefill.taskId).put("subject", prefill.subject).put("topic", prefill.topic).put("studyType", prefill.studyType).put("taskTitle", prefill.taskTitle).toString()).apply()
}

private fun loadPomodoroPrefill(context: Context): PomodoroPrefill? {
    val raw = context.getSharedPreferences("pomodoro_prefill", Context.MODE_PRIVATE).getString("next", null) ?: return null
    return runCatching { val json = JSONObject(raw); PomodoroPrefill(json.optLong("subjectId"), json.optLong("topicId"), json.optLong("taskId"), json.optString("subject"), json.optString("topic"), json.optString("studyType", "Revisão"), json.optString("taskTitle")) }.getOrNull()
}

private fun clearPomodoroPrefill(context: Context) { context.getSharedPreferences("pomodoro_prefill", Context.MODE_PRIVATE).edit().remove("next").apply() }
private fun reviewDaysForDifficulty(difficulty: String): Int = when (difficulty.lowercase(Locale.getDefault())) { "difícil", "dificil", "hard" -> 1; "fácil", "facil", "easy" -> 7; else -> 3 }
private fun String.toDifficultyCode(): String = when (lowercase(Locale.getDefault())) { "difícil", "dificil" -> "HARD"; "fácil", "facil" -> "EASY"; else -> "OK" }
private fun pomodoroReviewDateAfter(days: Int): String { val cal = Calendar.getInstance(); cal.add(Calendar.DAY_OF_YEAR, days); return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time) }

private const val PENDING_POMODORO_PREFS = "offline_pomodoro_queue"
private const val PENDING_POMODORO_KEY = "sessions"

private fun savePendingPomodoro(context: Context, subject: String, minutes: Int, content: String = "", studyType: String = "Revisão") {
    val prefs = context.getSharedPreferences(PENDING_POMODORO_PREFS, Context.MODE_PRIVATE)
    val current = JSONArray(prefs.getString(PENDING_POMODORO_KEY, "[]"))
    current.put(
        JSONObject()
            .put("subject", subject.ifBlank { "Pomodoro" })
            .put("minutes", minutes.coerceAtLeast(1))
            .put("content", content)
            .put("studyType", studyType.toBackendStudyType())
            .put("createdAt", System.currentTimeMillis())
    )
    prefs.edit().putString(PENDING_POMODORO_KEY, current.toString()).apply()
}

private suspend fun syncPendingPomodoros(
    context: Context,
    api: EducationalApi,
    userId: Long,
    onSynced: (Int) -> Unit
) {
    val prefs = context.getSharedPreferences(PENDING_POMODORO_PREFS, Context.MODE_PRIVATE)
    val current = JSONArray(prefs.getString(PENDING_POMODORO_KEY, "[]"))
    if (current.length() == 0) {
        onSynced(0)
        return
    }

    val remaining = JSONArray()
    var synced = 0
    for (index in 0 until current.length()) {
        val item = current.getJSONObject(index)
        val subject = item.optString("subject", "Pomodoro")
        val minutes = item.optInt("minutes", 1).coerceAtLeast(1)
        val content = item.optString("content", "Pomodoro offline")
        val studyType = item.optString("studyType", "REVIEW")
        val result = runCatching {
            api.createManualSession(
                userId = userId,
                subject = subject,
                durationMinutes = minutes,
                studyType = studyType,
                content = content,
                objective = "Sessao sincronizada depois de usar offline",
                notes = "Criada localmente no Pomodoro e sincronizada depois.",
                learned = "",
                difficulties = "",
                nextAction = "",
                focusRating = 4
            )
        }
        if (result.isSuccess) {
            synced += 1
        } else {
            remaining.put(item)
        }
    }
    prefs.edit().putString(PENDING_POMODORO_KEY, remaining.toString()).apply()
    onSynced(synced)
}

@Composable
fun PomodoroTimerCircle(time: String, label: String, accent: Color, progress: Float) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(260.dp)) {
            drawCircle(
                color = accent.copy(alpha = 0.14f),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 28f, cap = StrokeCap.Round)
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * clampedProgress,
                useCenter = false,
                topLeft = Offset(size.width * 0.05f, size.height * 0.05f),
                size = Size(size.width * 0.9f, size.height * 0.9f),
                style = Stroke(width = 28f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = time,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 58.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PomodoroColorSelector(selected: Color, onSelected: (Color) -> Unit) {
    val colors = listOf(LearnerPrimary, LearnerTeal, LearnerCoral, LearnerYellow)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(if (color == selected) 32.dp else 26.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onSelected(color) }
            )
        }
    }
}

@Composable
private fun PomodoroPlanChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) LearnerPrimary else LearnerPrimaryTint)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else LearnerPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}


