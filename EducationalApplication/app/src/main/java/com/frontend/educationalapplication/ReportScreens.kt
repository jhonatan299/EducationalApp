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
import androidx.compose.ui.platform.LocalContext
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
            ReportSectionCard(
                title = "Planejamento",
                subtitle = "Organize matérias, tópicos e provas",
                action = "Abrir",
                selected = selectedSection == 2,
                accent = LearnerTeal,
                onClick = { selectedSection = 2 }
            )
        }

        selectedSection?.let { section ->
            ReportSubscreenHeader(
                title = when (section) {
                    0 -> "Relatório da sessão"
                    1 -> "Revisões"
                    else -> "Planejamento de Estudo"
                },
                onBack = { selectedSection = null }
            )
            when (section) {
                0 -> HistoryScreen(api, userId, sessions, onDone)
                1 -> ReviewsScreen(api, userId, reviews, onDone)
                2 -> StudyPlanningScreen(userId = userId)
            }
            BottomBackButton(onBack = { selectedSection = null })
        }
    }
}

@Composable
fun ReportSubscreenHeader(title: String, onBack: () -> Unit) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
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
    var subject by remember { mutableStateOf("Hist?ria") }
    var minutes by remember { mutableStateOf("30") }
    var studyType by remember { mutableStateOf("Leitura") }
    var content by remember { mutableStateOf("") }
    var objective by remember { mutableStateOf("Entender o tema da aula") }
    var notes by remember { mutableStateOf("") }
    var learned by remember { mutableStateOf("") }
    var difficulties by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }
    var focusRating by remember { mutableStateOf("4") }
    var exerciseCount by remember { mutableStateOf("") }
    var correctAnswers by remember { mutableStateOf("") }
    var scheduleReviews by remember { mutableStateOf(true) }
    var filterSubject by remember { mutableStateOf("Todas") }
    var filterTopic by remember { mutableStateOf("Todos") }
    var filterType by remember { mutableStateOf("Todos") }
    var filterPeriod by remember { mutableStateOf("Todos") }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val plannedSubjects = remember(userId) { loadStudyPlanningSubjects(context, userId) }
    val selectedPlan = plannedSubjects.firstOrNull { it.name == subject }
    val topics = selectedPlan?.topics.orEmpty()
    val studyTypes = listOf("Leitura", "Exerc?cio", "Revis?o", "Resumo", "Videoaula", "Simulado", "Outro")

    SectionCard("Relat?rio da sess?o") {
        if (plannedSubjects.isNotEmpty()) {
            Text("Usar mat?ria do Planejamento (opcional)", color = LearnerMutedText, fontWeight = FontWeight.Bold)
            plannedSubjects.forEach { plan ->
                PlanningQuickChip(plan.name, subject == plan.name) {
                    subject = plan.name
                    content = ""
                }
            }
            if (topics.isNotEmpty()) {
                Text("T?pico/conte?do (opcional)", color = LearnerMutedText, fontWeight = FontWeight.Bold)
                topics.forEach { topic ->
                    PlanningQuickChip(topic.name, content == topic.name) { content = topic.name }
                }
            }
        }
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Mat?ria (opcional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("T?pico/conte?do (opcional)") }, modifier = Modifier.fillMaxWidth())
        Text("Tipo de estudo", color = LearnerMutedText, fontWeight = FontWeight.Bold)
        studyTypes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { type ->
                    PlanningQuickChip(type, studyType == type, Modifier.weight(1f)) { studyType = type }
                }
            }
        }
        NumberField("Minutos", minutes, { minutes = it }, Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            NumberField("Exerc?cios", exerciseCount, { exerciseCount = it }, Modifier.weight(1f))
            NumberField("Acertos", correctAnswers, { correctAnswers = it }, Modifier.weight(1f))
        }
        OutlinedTextField(value = objective, onValueChange = { objective = it }, label = { Text("Objetivo antes de estudar") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Anota??es r?pidas") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = learned, onValueChange = { learned = it }, label = { Text("O que aprendi") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = difficulties, onValueChange = { difficulties = it }, label = { Text("Dificuldades") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = nextAction, onValueChange = { nextAction = it }, label = { Text("Pr?xima a??o") }, modifier = Modifier.fillMaxWidth())
        NumberField("Foco de 1 a 5", focusRating, { focusRating = it.take(1) }, Modifier.fillMaxWidth())
        PlanningQuickChip(
            text = if (scheduleReviews) "✓ Agendar revisoes: 1, 3, 7 e 14 dias" else "+ Agendar revisoes",
            selected = scheduleReviews,
            modifier = Modifier.fillMaxWidth()
        ) { scheduleReviews = !scheduleReviews }
        Text("Anexos de imagem/?udio: preparado para fase futura.", color = LearnerMutedText, fontSize = 12.sp)
        Button(
            onClick = {
                val draft = PendingStudySessionDraft(
                    subject = subject.ifBlank { "Sem mat?ria" },
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
                scope.apiCall(
                    action = {
                        val saved = api.createManualSession(
                            userId = userId,
                            subject = draft.subject,
                            durationMinutes = draft.durationMinutes,
                            studyType = draft.studyType,
                            content = draft.content,
                            objective = draft.objective,
                            notes = draft.notes,
                            learned = draft.learned,
                            difficulties = draft.difficulties,
                            nextAction = draft.nextAction,
                            focusRating = draft.focusRating,
                            exerciseCount = exerciseCount.toIntOrNull(),
                            correctAnswers = correctAnswers.toIntOrNull()
                        )
                        if (scheduleReviews) {
                            listOf(1, 3, 7, 14).forEach { days ->
                                api.createReview(
                                    userId = userId,
                                    title = "Revisar ${content.ifBlank { subject }}",
                                    subject = subject.ifBlank { "Sem matéria" },
                                    dueDate = reviewDateAfter(days)
                                )
                            }
                        }
                        saved
                    },
                    onSuccess = {
                        status = "Relat?rio salvo. Revis?o criada para essa sess?o."
                        onDone()
                    },
                    onError = {
                        savePendingStudySession(context, draft)
                        status = "Sem internet: relat?rio salvo localmente para sincronizar depois."
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Salvar sess?o") }
        status?.let { Text(it, color = LearnerMutedText, fontWeight = FontWeight.Bold) }
    }

    val filteredSessions = sessions.filter { session ->
        val subjectOk = filterSubject == "Todas" || session.subject == filterSubject
        val topicOk = filterTopic == "Todos" || (session.content ?: "") == filterTopic
        val typeOk = filterType == "Todos" || session.studyType.toStudyTypeLabel() == filterType
        val periodOk = session.matchesPeriod(filterPeriod)
        subjectOk && topicOk && typeOk && periodOk
    }

    SectionCard("Hist?rico") {
        Text("Filtros", fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = filterSubject, onValueChange = { filterSubject = it }, label = { Text("Mat?ria") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = filterTopic, onValueChange = { filterTopic = it }, label = { Text("T?pico") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = filterType, onValueChange = { filterType = it }, label = { Text("Tipo") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = filterPeriod, onValueChange = { filterPeriod = it }, label = { Text("Todos/Semana/M?s/Ano") }, modifier = Modifier.weight(1f))
        }
        if (filteredSessions.isEmpty()) {
            EmptyStateCard(
                title = "Nenhum relat?rio encontrado",
                subtitle = "Salve uma sess?o ou ajuste os filtros."
            )
        } else {
            filteredSessions.forEach { session ->
                StudySessionReportCard(
                    session = session,
                    onDelete = {
                        scope.apiCall(
                            action = { api.deleteStudySession(session.id) },
                            onSuccess = {
                                status = "Relat?rio eliminado."
                                onDone()
                            },
                            onError = { status = it }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PlanningQuickChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) LearnerPrimary else LearnerPrimaryTint)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else LearnerPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

private fun StudySessionResponse.matchesPeriod(period: String): Boolean {
    val normalized = period.trim().lowercase()
    if (normalized == "todos" || normalized.isBlank()) return true
    val date = (endedAt ?: startedAt)?.take(10) ?: return true
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sessionDate = runCatching { parser.parse(date) }.getOrNull() ?: return true
    val cal = Calendar.getInstance()
    val now = cal.time
    cal.time = now
    when (normalized) {
        "semana" -> cal.add(Calendar.DAY_OF_YEAR, -7)
        "m?s", "mes" -> cal.add(Calendar.MONTH, -1)
        "ano" -> cal.add(Calendar.YEAR, -1)
        else -> return true
    }
    return sessionDate.after(cal.time)
}

private fun reviewDateAfter(days: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

@Composable
private fun StudySessionReportCard(
    session: StudySessionResponse,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = session.subject.ifBlank { "Sem mat?ria" },
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
            Text(
                text = session.content ?: "Sem t?pico",
                color = LearnerMutedText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${session.durationMinutes} min ? ${session.studyType.toStudyTypeLabel() ?: session.source.toSessionSourceLabel()} ? foco ${session.focusRating ?: "-"}",
                color = LearnerMutedText
            )
            if (session.exerciseCount != null || session.correctAnswers != null) {
                Text(
                    text = "Exerc?cios: ${session.correctAnswers ?: 0}/${session.exerciseCount ?: 0}",
                    color = LearnerTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Pr?xima a??o: ${session.nextAction ?: "nenhuma"}",
                color = LearnerMutedText,
                fontSize = 13.sp
            )
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar relat?rio")
            }
        }
    }
}

