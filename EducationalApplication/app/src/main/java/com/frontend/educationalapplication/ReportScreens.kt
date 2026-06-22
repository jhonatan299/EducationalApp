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
import com.frontend.educationalapplication.ui.theme.CircularDisplayFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
@Composable
fun ReportReviewsScreen(
    api: EducationalApi,
    userId: Long,
    sessions: List<StudySessionResponse>,
    reviews: List<ReviewReminderResponse>,
    onDone: () -> Unit
) {
    var selectedSection by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (selectedSection == null) {
        SectionHeader(
            title = "Relatório",
            subtitle = "Registre estudos e acompanhe suas revisões"
        )
        ReportSectionCard(
            title = "Relatório da sessão",
            subtitle = "Anote o que estudou",
            action = "Abrir",
            selected = selectedSection == 0,
            accent = Color(0xFF6A63FF),
            onClick = { selectedSection = 0 }
        )
        ReportSectionCard(
            title = "Revisões",
            subtitle = "${reviews.count { !it.completed }} pendentes",
            action = "Ver",
            selected = selectedSection == 1,
            accent = LearnerPrimary,
            onClick = { selectedSection = 1 }
        )

        }

        selectedSection?.let { section ->
            ReportSubscreenHeader(
                title = if (section == 0) "Relatório da sessão" else "Revisões",
                onBack = { selectedSection = null }
            )
            if (section == 0) {
                HistoryScreen(api, userId, sessions, onDone)
            } else {
                ReviewsScreen(api, reviews, onDone)
            }
        }
    }
}

@Composable
fun ReportSubscreenHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("Voltar", color = LearnerPrimary, fontWeight = FontWeight.Bold)
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun ReportSectionCard(
    title: String,
    subtitle: String,
    action: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accent else accent.copy(alpha = 0.82f))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color(0xFF243BCE).copy(alpha = 0.20f),
                radius = size.width * 0.18f,
                center = Offset(size.width * 0.58f, size.height * 1.02f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.width * 0.26f,
                center = Offset(size.width * 0.92f, size.height * 0.1f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.30f),
                topLeft = Offset(size.width * 0.58f, size.height * 0.26f),
                size = Size(size.width * 0.17f, size.height * 0.34f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 6f)
            )
            drawRoundRect(
                color = Color(0xFF201A73).copy(alpha = 0.55f),
                topLeft = Offset(size.width * 0.72f, size.height * 0.44f),
                size = Size(size.width * 0.17f, size.height * 0.34f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 6f)
            )
        }
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = CircularDisplayFamily
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontFamily = CircularDisplayFamily
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White)
                    .width(98.dp)
                    .height(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action,
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CircularDisplayFamily
                )
            }
        }
    }
}

@Composable
fun HistoryScreen(
    api: EducationalApi,
    userId: Long,
    sessions: List<StudySessionResponse>,
    onDone: () -> Unit
) {
    // This screen creates a richer study session report, not only "minutes studied".
    var subject by remember { mutableStateOf("História") }
    var minutes by remember { mutableStateOf("30") }
    var studyType by remember { mutableStateOf("Teoria") }
    var content by remember { mutableStateOf("Brasil República") }
    var objective by remember { mutableStateOf("Entender o tema da aula") }
    var notes by remember { mutableStateOf("") }
    var learned by remember { mutableStateOf("") }
    var difficulties by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }
    var focusRating by remember { mutableStateOf("4") }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    SectionCard("Relatório da sessão") {
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Matéria") })
        NumberField("Minutos", minutes, { minutes = it }, Modifier.fillMaxWidth())
        OutlinedTextField(value = studyType, onValueChange = { studyType = it.uppercase() }, label = { Text("Tipo de estudo") })
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Conteúdo") })
        OutlinedTextField(value = objective, onValueChange = { objective = it }, label = { Text("Objetivo antes de estudar") })
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Anotações rápidas") })
        OutlinedTextField(value = learned, onValueChange = { learned = it }, label = { Text("O que aprendi") })
        OutlinedTextField(value = difficulties, onValueChange = { difficulties = it }, label = { Text("Dificuldades") })
        OutlinedTextField(value = nextAction, onValueChange = { nextAction = it }, label = { Text("Próxima ação") })
        NumberField("Foco de 1 a 5", focusRating, { focusRating = it.take(1) }, Modifier.fillMaxWidth())
        Button(
            onClick = {
                scope.apiCall(
                    action = {
                        // Sends the report fields to Spring Boot.
                        // The backend also creates a review reminder automatically.
                        api.createManualSession(
                            userId = userId,
                            subject = subject,
                            durationMinutes = minutes.toIntOrNull() ?: 1,
                            studyType = studyType.toBackendStudyType(),
                            content = content,
                            objective = objective,
                            notes = notes,
                            learned = learned,
                            difficulties = difficulties,
                            nextAction = nextAction,
                            focusRating = (focusRating.toIntOrNull() ?: 3).coerceIn(1, 5)
                        )
                    },
                    onSuccess = {
                        status = "Relatório salvo. Lembrete de revisão criado."
                        onDone()
                    },
                    onError = { status = it }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar sessão")
        }
        status?.let { Text(it) }
    }

    SectionCard("Histórico") {
        sessions.forEach { session ->
            ListLine(
                title = "${session.subject} - ${session.content ?: session.studyType.toStudyTypeLabel() ?: session.source.toSessionSourceLabel()}",
                detail = "${session.durationMinutes} min - foco ${session.focusRating ?: "-"} - próxima ação: ${session.nextAction ?: "nenhuma"}"
            )
        }
    }
}


