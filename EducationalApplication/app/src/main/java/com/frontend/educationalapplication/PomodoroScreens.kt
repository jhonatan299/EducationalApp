package com.frontend.educationalapplication

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
@Composable
fun PomodoroDetailScreen(
    api: EducationalApi,
    userId: Long,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReportSubscreenHeader(
            title = "Pomodoro",
            onBack = onBack
        )
        PomodoroScreen(api = api, userId = userId, onDone = onDone)
    }
}

@Composable
fun PomodoroScreen(api: EducationalApi, userId: Long, onDone: () -> Unit) {
    // Pomodoro screen state: input values, timer value, and current backend session id.
    var subject by remember { mutableStateOf("Matemática") }
    var focusMinutes by remember { mutableStateOf("25") }
    var breakMinutes by remember { mutableStateOf("5") }
    var pomodoroId by remember { mutableLongStateOf(0L) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var totalSeconds by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Pronto") }
    var timerAccent by remember { mutableStateOf(LearnerPrimary) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(running) {
        // Simple local countdown. The backend stores the session, but the ticking happens here.
        while (running && remainingSeconds > 0) {
            delay(1.seconds)
            remainingSeconds -= 1
        }
        if (running && remainingSeconds == 0 && pomodoroId != 0L) {
            running = false
            status = "Foco concluído"
        }
    }

    SectionCard("Pomodoro") {
        PomodoroTimerCircle(
            time = timerText(remainingSeconds),
            label = if (running) "FOCO" else status.uppercase(),
            accent = timerAccent,
            progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
        )
        PomodoroColorSelector(
            selected = timerAccent,
            onSelected = { timerAccent = it }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Matéria") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField("Foco", focusMinutes, { focusMinutes = it }, Modifier.weight(1f))
            NumberField("Pausa", breakMinutes, { breakMinutes = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val focus = focusMinutes.toIntOrNull() ?: 25
                    val pause = breakMinutes.toIntOrNull() ?: 5
                    // Start a Pomodoro on the backend, then use its id for extend/complete.
                    scope.apiCall(
                        action = { api.startPomodoro(userId, subject, focus, pause) },
                        onSuccess = {
                            pomodoroId = it.id
                            totalSeconds = it.totalFocusMinutes * 60
                            remainingSeconds = totalSeconds
                            running = true
                            status = "Em andamento"
                        },
                        onError = { status = it }
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Começar")
            }
            Button(onClick = { running = !running }, modifier = Modifier.weight(1f)) {
                Text(if (running) "Pausar" else "Continuar")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (pomodoroId != 0L) {
                        // Extends both backend session and local countdown.
                        scope.apiCall(
                            action = { api.extendPomodoro(pomodoroId) },
                            onSuccess = {
                                totalSeconds += 5 * 60
                                remainingSeconds += 5 * 60
                                status = "+5 minutos"
                            },
                            onError = { status = it }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("+5")
            }
            Button(
                onClick = {
                    if (pomodoroId != 0L) {
                        // Completing creates a study history entry in the backend.
                        scope.apiCall(
                            action = { api.completePomodoro(pomodoroId, "Concluído no protótipo Android") },
                            onSuccess = {
                                running = false
                                status = "Salvo"
                                onDone()
                            },
                            onError = { status = it }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Concluir")
            }
        }
    }
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


