package com.frontend.educationalapplication

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    onDone: () -> Unit,
    onStartStudy: () -> Unit = {},
    initialSection: Int? = null
) {
    var selectedSection by remember { mutableStateOf<Int?>(initialSection) }

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
            ReportSectionCard(
                title = "Planejamento inteligente",
                subtitle = "Crie um plano de revisões com IA",
                action = "Gerar",
                selected = selectedSection == 3,
                accent = Color(0xFF292059),
                onClick = { selectedSection = 3 }
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
                1 -> ReviewsScreen(api, userId, reviews, onDone, onStartStudy)
                2 -> StudyPlanningScreen(userId = userId, onStartStudy = onStartStudy)
                3 -> SmartRevisionRoadmapScreen(api = api, userId = userId, onSaved = onDone)
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
    var subject by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("30") }
    var studyType by remember { mutableStateOf("Leitura") }
    var content by remember { mutableStateOf("") }
    var objective by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var learned by remember { mutableStateOf("") }
    var difficulties by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }
    var focusRating by remember { mutableStateOf("") }
    var exerciseCount by remember { mutableStateOf("") }
    var correctAnswers by remember { mutableStateOf("") }
    var selectedReviewDays by remember { mutableStateOf(setOf(1, 3, 7, 14)) }
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
    val studyTypes = listOf("Leitura", "Exercício", "Revisão", "Resumo", "Videoaula", "Simulado", "Outro")
    val canSave = (minutes.toIntOrNull() ?: 0) > 0 && studyType.isNotBlank()
    var reportMode by remember { mutableStateOf<Int?>(null) }

    if (reportMode == null) {
        ReportSectionCard(
            title = "Criar relatório",
            subtitle = "Registrar uma nova sessão de estudo",
            action = "Criar",
            selected = false,
            accent = Color(0xFF6A63FF),
            onClick = { reportMode = 0 }
        )
        ReportSectionCard(
            title = "Histórico de relatórios",
            subtitle = "${sessions.size} sessões registradas",
            action = "Ver",
            selected = false,
            accent = LearnerTeal,
            onClick = { reportMode = 1 }
        )
        return
    }

    TextButton(onClick = { reportMode = null }) {
        Text("← Voltar ao menu de relatório")
    }


    if (reportMode == 0) {
        var selectingSubject by remember { mutableStateOf(false) }
        var selectingTopic by remember { mutableStateOf(false) }
        var difficulty by remember { mutableStateOf("Ok") }
        var imageAttachment by remember { mutableStateOf<Uri?>(null) }
        var audioAttachment by remember { mutableStateOf<Uri?>(null) }
        val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageAttachment = uri }
        val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> audioAttachment = uri }

        if (selectingSubject) {
            SubjectSelectionPanel(
                context = context,
                userId = userId,
                selectedSubject = subject,
                onSubjectSelected = {
                    subject = it
                    content = ""
                    selectingSubject = false
                },
                onBack = { selectingSubject = false }
            )
            return
        }

        if (selectingTopic) {
            TopicSelectionPanel(
                context = context,
                userId = userId,
                subject = subject,
                selectedTopic = content,
                onTopicSelected = {
                    content = it
                    selectingTopic = false
                },
                onBack = { selectingTopic = false }
            )
            return
        }

        SectionCard("Salvar sessão") {
            Text("Tempo estudado", color = LearnerMutedText, fontWeight = FontWeight.Bold)
            NumberField("Minutos", minutes, { minutes = it.filter { char -> char.isDigit() }.take(3) }, Modifier.fillMaxWidth())

            SimpleSelectionCard(
                title = "Matéria",
                value = subject.ifBlank { "Sem matéria" },
                action = "Selecionar matéria",
                onClick = { selectingSubject = true }
            )

            if (subject.isNotBlank()) {
                SimpleSelectionCard(
                    title = "Tema",
                    value = content.ifBlank { "Pular tema" },
                    action = "Selecionar tema",
                    onClick = { selectingTopic = true }
                )
            }

            Text("Como foi?", color = LearnerMutedText, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Difícil", "Ok", "Fácil").forEach { option ->
                    PlanningQuickChip(option, difficulty == option, Modifier.weight(1f)) { difficulty = option }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Nota rápida") },
                placeholder = { Text("Escreva algo que queira lembrar") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Anexos", color = LearnerMutedText, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.weight(1f)) { Text(if (imageAttachment == null) "Imagem" else "Imagem anexada") }
                Button(onClick = { audioPicker.launch("audio/*") }, modifier = Modifier.weight(1f)) { Text(if (audioAttachment == null) "Áudio" else "Áudio anexado") }
            }
            Text("Os anexos ficam vinculados localmente à sessão; upload para nuvem fica para uma próxima fase.", color = LearnerMutedText, fontSize = 12.sp)

            Button(enabled = canSave, onClick = {
                val savedSubject = subject.ifBlank { "Sem matéria" }
                val mappedFocus = when (difficulty) {
                    "Difícil" -> 2
                    "Fácil" -> 5
                    else -> 4
                }
                val attachmentNotes = listOfNotNull(
                    imageAttachment?.let { "Imagem anexada: $it" },
                    audioAttachment?.let { "Áudio anexado: $it" }
                )
                val finalNotes = listOf("Dificuldade: $difficulty", notes.takeIf { it.isNotBlank() })
                    .filterNotNull()
                    .plus(attachmentNotes)
                    .joinToString(" | ")
                val draft = PendingStudySessionDraft(
                    savedSubject,
                    minutes.toIntOrNull() ?: 1,
                    "REVIEW",
                    content,
                    "",
                    finalNotes,
                    "",
                    "",
                    "",
                    mappedFocus
                )
                scope.apiCall(
                    action = { api.createManualSession(userId, draft.subject, draft.durationMinutes, draft.studyType, draft.content, draft.objective, draft.notes, draft.learned, draft.difficulties, draft.nextAction, draft.focusRating, null, null) },
                    onSuccess = {
                        status = "Sessão salva."
                        onDone()
                        reportMode = 1
                    },
                    onError = {
                        savePendingStudySession(context, draft)
                        recordMissionStudySession(context, userId, draft.durationMinutes, draft.subject)
                        status = "Sem internet: sessão salva localmente."
                        reportMode = 1
                    }
                )
            }, modifier = Modifier.fillMaxWidth()) { Text("Salvar sessão") }

            if (!canSave) Text("Informe o tempo estudado.", color = LearnerCoral, fontSize = 12.sp)
            status?.let { Text(it, color = LearnerMutedText, fontWeight = FontWeight.Bold) }
        }
    }

    val filteredSessions = sessions.filter { session ->
        val subjectOk = filterSubject == "Todas" || session.subject == filterSubject
        val topicOk = filterTopic == "Todos" || (session.content ?: "") == filterTopic
        val typeOk = filterType == "Todos" || session.studyType.toStudyTypeLabel() == filterType
        val periodOk = session.matchesPeriod(filterPeriod)
        subjectOk && topicOk && typeOk && periodOk
    }
    if (reportMode == 1) SectionCard("Histórico de relatórios") {
        Text("Filtros", fontWeight = FontWeight.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = filterSubject, onValueChange = { filterSubject = it }, label = { Text("Matéria") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = filterTopic, onValueChange = { filterTopic = it }, label = { Text("Tópico") }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = filterType, onValueChange = { filterType = it }, label = { Text("Tipo") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = filterPeriod, onValueChange = { filterPeriod = it }, label = { Text("Todos/Semana/Mês/Ano") }, modifier = Modifier.weight(1f))
        }
        if (filteredSessions.isEmpty()) EmptyStateCard("Nenhum relatório encontrado", "Salve uma sessão ou ajuste os filtros.") else filteredSessions.forEach { session ->
            StudySessionReportCard(session = session, onDelete = { scope.apiCall(action = { api.deleteStudySession(session.id) }, onSuccess = { status = "Relatório eliminado."; onDone() }, onError = { status = it }) })
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
        "mês", "mes" -> cal.add(Calendar.MONTH, -1)
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
private fun SimpleSelectionCard(
    title: String,
    value: String,
    action: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LearnerPrimaryTint.copy(alpha = .55f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = LearnerMutedText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
            }
            Text(action, color = LearnerPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SubjectSelectionPanel(
    context: Context,
    userId: Long,
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var base by remember { mutableStateOf(SubjectBases.HIGH_SCHOOL) }
    var customName by remember { mutableStateOf("") }
    var refreshKey by remember { mutableIntStateOf(0) }
    val subjects = remember(base, query, refreshKey, userId) { searchSubjects(context, base, query, userId) }

    SectionCard("Selecionar matéria") {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar matéria") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(SubjectBases.HIGH_SCHOOL, SubjectBases.COMPUTING, SubjectBases.CUSTOM).forEach { option ->
                PlanningQuickChip(option, base == option, Modifier.weight(1f)) { base = option }
            }
        }
        PlanningQuickChip("Sem matéria", selectedSubject.isBlank(), Modifier.fillMaxWidth()) { onSubjectSelected("") }
        subjects.forEach { subject ->
            PlanningQuickChip(subject.name, selectedSubject == subject.name, Modifier.fillMaxWidth()) { onSubjectSelected(subject.name) }
        }
        if (subjects.isEmpty()) {
            EmptyStateCard("Nada encontrado", "Tente outra busca ou crie uma matéria personalizada.")
        }
        Text("Criar matéria", color = LearnerMutedText, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = customName,
                onValueChange = { customName = it },
                label = { Text("Nova matéria") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                addCustomSubjectToPlanning(context, userId, customName)
                onSubjectSelected(customName.trim())
                customName = ""
                refreshKey += 1
            }, enabled = customName.trim().isNotBlank()) { Text("+") }
        }
        BottomBackButton(onBack = onBack)
    }
}

@Composable
private fun TopicSelectionPanel(
    context: Context,
    userId: Long,
    subject: String,
    selectedTopic: String,
    onTopicSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var customTopic by remember { mutableStateOf("") }
    var refreshKey by remember { mutableIntStateOf(0) }
    val topics = remember(subject, query, refreshKey, userId) { searchTopics(context, subject, query, userId) }

    SectionCard("Selecionar tema") {
        Text(subject, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar tema") },
            modifier = Modifier.fillMaxWidth()
        )
        PlanningQuickChip("Pular tema", selectedTopic.isBlank(), Modifier.fillMaxWidth()) { onTopicSelected("") }
        topics.forEach { topic ->
            PlanningQuickChip(topic, selectedTopic == topic, Modifier.fillMaxWidth()) { onTopicSelected(topic) }
        }
        if (topics.isEmpty()) {
            EmptyStateCard("Nenhum tema encontrado", "Crie um tema personalizado para essa matéria.")
        }
        Text("Criar tema", color = LearnerMutedText, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = customTopic,
                onValueChange = { customTopic = it },
                label = { Text("Novo tema") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                addCustomTopicToPlanning(context, userId, subject, customTopic)
                onTopicSelected(customTopic.trim())
                customTopic = ""
                refreshKey += 1
            }, enabled = customTopic.trim().isNotBlank()) { Text("+") }
        }
        BottomBackButton(onBack = onBack)
    }
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
                text = session.subject.ifBlank { "Sem matéria" },
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
            Text(
                text = session.content ?: "Sem tópico",
                color = LearnerMutedText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${session.durationMinutes} min • ${session.studyType.toStudyTypeLabel() ?: session.source.toSessionSourceLabel()} • foco ${session.focusRating ?: "-"}",
                color = LearnerMutedText
            )
            if (session.exerciseCount != null || session.correctAnswers != null) {
                Text(
                    text = "Exercícios: ${session.correctAnswers ?: 0}/${session.exerciseCount ?: 0}",
                    color = LearnerTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Próxima ação: ${session.nextAction ?: "nenhuma"}",
                color = LearnerMutedText,
                fontSize = 13.sp
            )
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar relatório")
            }
        }
    }
}
