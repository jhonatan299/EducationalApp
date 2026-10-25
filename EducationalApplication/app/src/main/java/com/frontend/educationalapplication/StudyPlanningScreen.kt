package com.frontend.educationalapplication

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frontend.educationalapplication.ui.theme.CircularDisplayFamily
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import com.frontend.educationalapplication.ui.theme.LearnerTeal
import org.json.JSONArray
import org.json.JSONObject

enum class SubjectPriority { LOW, MEDIUM, HIGH }
enum class StudyDifficulty { EASY, MEDIUM, HARD }
enum class TopicStatus { NOT_STARTED, IN_PROGRESS, STUDIED, NEEDS_REVIEW, MASTERED }
enum class StudyTaskStatus { PENDING, COMPLETED }

data class StudySubjectPlan(
    val id: Long,
    val name: String,
    val priority: SubjectPriority,
    val difficulty: StudyDifficulty,
    val examDate: String,
    val estimatedHours: String,
    val weeklyGoalMinutes: String,
    val createdAt: Long,
    val topics: List<StudyTopicPlan> = emptyList()
)

data class StudyTopicPlan(
    val id: Long,
    val subjectId: Long,
    val name: String,
    val status: TopicStatus,
    val difficulty: StudyDifficulty?,
    val estimatedMinutes: String,
    val note: String,
    val createdAt: Long,
    val tasks: List<StudyTaskPlan> = emptyList()
)

data class StudyTaskPlan(
    val id: Long,
    val title: String,
    val studyType: String = "Leitura",
    val status: StudyTaskStatus = StudyTaskStatus.PENDING,
    val orderIndex: Int = 0,
    val completedAt: Long? = null
)

private enum class PlanningMode { LIST, SUBJECT_FORM, DETAILS, TOPIC_FORM }

@Composable
fun StudyPlanningScreen(userId: Long, onStartStudy: () -> Unit = {}) {
    val context = LocalContext.current
    var subjects by remember(userId) { mutableStateOf(loadStudyPlanningSubjects(context, userId)) }
    var mode by remember { mutableStateOf(PlanningMode.LIST) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var editingSubject by remember { mutableStateOf<StudySubjectPlan?>(null) }
    var editingTopic by remember { mutableStateOf<StudyTopicPlan?>(null) }

    fun persist(next: List<StudySubjectPlan>) {
        subjects = next
        saveStudyPlanningSubjects(context, userId, next)
    }

    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        when (mode) {
            PlanningMode.LIST -> StudyPlanningList(
                subjects = subjects,
                onCreate = {
                    editingSubject = null
                    mode = PlanningMode.SUBJECT_FORM
                },
                onOpen = {
                    selectedSubjectId = it.id
                    mode = PlanningMode.DETAILS
                }
            )

            PlanningMode.SUBJECT_FORM -> StudySubjectForm(
                initial = editingSubject,
                onCancel = { mode = if (selectedSubjectId == null) PlanningMode.LIST else PlanningMode.DETAILS },
                onSave = { saved ->
                    val next = if (subjects.any { it.id == saved.id }) {
                        subjects.map { if (it.id == saved.id) saved.copy(topics = it.topics) else it }
                    } else {
                        subjects + saved
                    }
                    persist(next)
                    selectedSubjectId = saved.id
                    editingSubject = null
                    mode = PlanningMode.DETAILS
                }
            )

            PlanningMode.DETAILS -> {
                if (selectedSubject == null) {
                    EmptyPlanningCard("Matéria não encontrada", "Volte para a lista e escolha outra matéria.", "Voltar") {
                        mode = PlanningMode.LIST
                    }
                } else {
                    StudySubjectDetails(
                        subject = selectedSubject,
                        onAddTopic = {
                            editingTopic = null
                            mode = PlanningMode.TOPIC_FORM
                        },
                        onEditSubject = {
                            editingSubject = selectedSubject
                            mode = PlanningMode.SUBJECT_FORM
                        },
                        onDeleteSubject = {
                            persist(subjects.filterNot { it.id == selectedSubject.id })
                            selectedSubjectId = null
                            mode = PlanningMode.LIST
                        },
                        onBackToList = { mode = PlanningMode.LIST },
                        onStatus = { topic, status ->
                            persist(subjects.map { subject ->
                                if (subject.id == selectedSubject.id) {
                                    subject.copy(topics = subject.topics.map { if (it.id == topic.id) it.copy(status = status) else it })
                                } else subject
                            })
                        },
                        onEditTopic = {
                            editingTopic = it
                            mode = PlanningMode.TOPIC_FORM
                        },
                        onDeleteTopic = { topic ->
                            persist(subjects.map { subject ->
                                if (subject.id == selectedSubject.id) {
                                    subject.copy(topics = subject.topics.filterNot { it.id == topic.id })
                                } else subject
                            })
                        },
                        onStartTask = { topic, task ->
                            savePomodoroPrefill(
                                context,
                                PomodoroPrefill(
                                    subjectId = selectedSubject.id,
                                    topicId = topic.id,
                                    taskId = task.id,
                                    subject = selectedSubject.name,
                                    topic = topic.name,
                                    studyType = task.studyType,
                                    taskTitle = task.title
                                )
                            )
                            onStartStudy()
                        }
                    )
                }
            }

            PlanningMode.TOPIC_FORM -> {
                if (selectedSubject == null) {
                    mode = PlanningMode.LIST
                } else {
                    StudyTopicForm(
                        subjectId = selectedSubject.id,
                        initial = editingTopic,
                        onCancel = { mode = PlanningMode.DETAILS },
                        onSave = { topic ->
                            persist(subjects.map { subject ->
                                if (subject.id == selectedSubject.id) {
                                    val topics = if (subject.topics.any { it.id == topic.id }) {
                                        subject.topics.map { if (it.id == topic.id) topic else it }
                                    } else {
                                        subject.topics + topic.copy(tasks = defaultStudyTasks())
                                    }
                                    subject.copy(topics = topics)
                                } else subject
                            })
                            editingTopic = null
                            mode = PlanningMode.DETAILS
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyPlanningList(subjects: List<StudySubjectPlan>, onCreate: () -> Unit, onOpen: (StudySubjectPlan) -> Unit) {
    Text("Organize matérias, temas e prazos para estudar melhor.", color = LearnerMutedText)
    Button(shape = MaterialTheme.shapes.small, onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("+ Nova matéria") }
    if (subjects.isEmpty()) {
        EmptyPlanningCard(
            title = "Você ainda não criou nenhum plano de estudo.",
            text = "Cadastre uma matéria para organizar seus estudos.",
            action = "Criar matéria",
            onAction = onCreate
        )
    } else {
        subjects.forEach { StudySubjectCard(subject = it, onClick = { onOpen(it) }) }
    }
}

@Composable
private fun StudySubjectCard(subject: StudySubjectPlan, onClick: () -> Unit) {
    val total = subject.topics.size
    val done = subject.topics.count { it.status == TopicStatus.STUDIED || it.status == TopicStatus.MASTERED }
    val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(subject.name, fontFamily = CircularDisplayFamily, fontSize = 21.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PlanningPill("Prioridade: ${subject.priority.label()}", LearnerPrimary, Modifier.weight(1f))
                PlanningPill("Dificuldade: ${subject.difficulty.label()}", LearnerCoral, Modifier.weight(1f))
            }
            if (subject.examDate.isNotBlank()) Text("Prova/prazo: ${subject.examDate}", color = LearnerMutedText)
            if (subject.estimatedHours.isNotBlank()) Text("Horas estimadas: ${subject.estimatedHours}h", color = LearnerMutedText)
            Text("Temas: $done/$total concluídos", color = LearnerMutedText, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
                color = LearnerPrimary,
                trackColor = LearnerPrimaryTint,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
private fun StudySubjectDetails(
    subject: StudySubjectPlan,
    onAddTopic: () -> Unit,
    onEditSubject: () -> Unit,
    onDeleteSubject: () -> Unit,
    onBackToList: () -> Unit,
    onStatus: (StudyTopicPlan, TopicStatus) -> Unit,
    onEditTopic: (StudyTopicPlan) -> Unit,
    onDeleteTopic: (StudyTopicPlan) -> Unit,
    onStartTask: (StudyTopicPlan, StudyTaskPlan) -> Unit
) {
    StudySubjectCard(subject = subject, onClick = {})
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(shape = MaterialTheme.shapes.small, onClick = onAddTopic, modifier = Modifier.weight(1f)) { Text("Adicionar tema") }
        Button(shape = MaterialTheme.shapes.small, onClick = onEditSubject, modifier = Modifier.weight(1f)) { Text("Editar matéria") }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = onBackToList, modifier = Modifier.weight(1f)) { Text("Ver lista") }
        TextButton(onClick = onDeleteSubject, modifier = Modifier.weight(1f)) { Text("Excluir matéria", color = LearnerCoral) }
    }
    Text("Temas", fontWeight = FontWeight.Black, fontSize = 20.sp)
    if (subject.topics.isEmpty()) {
        EmptyPlanningCard("Nenhum tema cadastrado", "Adicione temas para acompanhar o progresso dessa matéria.", "Adicionar tema", onAddTopic)
    } else {
        subject.topics.forEach {
            StudyTopicCard(
                topic = it,
                onStatus = { status -> onStatus(it, status) },
                onEdit = { onEditTopic(it) },
                onDelete = { onDeleteTopic(it) },
                onStartTask = { task -> onStartTask(it, task) }
            )
        }
    }
}

@Composable
private fun StudyTopicCard(
    topic: StudyTopicPlan,
    onStatus: (TopicStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartTask: (StudyTaskPlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(topic.name, fontWeight = FontWeight.Black, fontSize = 17.sp)
            PlanningPill(topic.status.label(), LearnerTeal)
            topic.difficulty?.let { PlanningPill("Dificuldade: ${it.label()}", LearnerCoral) }
            if (topic.estimatedMinutes.isNotBlank()) Text("Tempo estimado: ${topic.estimatedMinutes} min", color = LearnerMutedText)
            if (topic.note.isNotBlank()) Text(topic.note, color = LearnerMutedText)
            Text("Checklist de estudo", fontWeight = FontWeight.Black)
            val tasks = topic.tasks.ifEmpty { defaultStudyTasks() }
            tasks.sortedBy { it.orderIndex }.forEach { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = LearnerPrimaryTint.copy(alpha = .55f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(task.title, fontWeight = FontWeight.Bold)
                        Text(task.studyType, color = LearnerMutedText, fontSize = 12.sp)
                        Button(shape = MaterialTheme.shapes.small, onClick = { onStartTask(task) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Iniciar estudo")
                        }
                    }
                }
            }
            listOf(TopicStatus.IN_PROGRESS, TopicStatus.STUDIED, TopicStatus.NEEDS_REVIEW, TopicStatus.MASTERED).chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { status ->
                        ReusablePlanningChip(status.label(), topic.status == status, Modifier.weight(1f)) { onStatus(status) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Editar") }
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Excluir", color = LearnerCoral) }
            }
        }
    }
}

@Composable
private fun StudySubjectForm(initial: StudySubjectPlan?, onCancel: () -> Unit, onSave: (StudySubjectPlan) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var priority by remember(initial?.id) { mutableStateOf(initial?.priority ?: SubjectPriority.MEDIUM) }
    var difficulty by remember(initial?.id) { mutableStateOf(initial?.difficulty ?: StudyDifficulty.MEDIUM) }
    var examDate by remember(initial?.id) { mutableStateOf(initial?.examDate.orEmpty()) }
    var estimatedHours by remember(initial?.id) { mutableStateOf(initial?.estimatedHours.orEmpty()) }
    var weeklyGoal by remember(initial?.id) { mutableStateOf(initial?.weeklyGoalMinutes.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    PlanningFormCard(if (initial == null) "Nova matéria" else "Editar matéria") {
        OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Nome da matéria") }, modifier = Modifier.fillMaxWidth())
        PlanningSelector("Prioridade", SubjectPriority.entries, priority, { it.label() }) { priority = it }
        PlanningSelector("Dificuldade", StudyDifficulty.entries, difficulty, { it.label() }) { difficulty = it }
        OutlinedTextField(value = examDate, onValueChange = { examDate = it }, label = { Text("Data da prova ou prazo (opcional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = estimatedHours, onValueChange = { estimatedHours = it }, label = { Text("Horas estimadas (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = weeklyGoal, onValueChange = { weeklyGoal = it }, label = { Text("Meta semanal em minutos (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = LearnerCoral, fontWeight = FontWeight.Bold) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(shape = MaterialTheme.shapes.small,
                onClick = {
                    if (name.isBlank()) {
                        error = "Nome da matéria é obrigatório para salvar."
                    } else {
                        onSave(
                            StudySubjectPlan(
                                id = initial?.id ?: System.currentTimeMillis(),
                                name = name.trim(),
                                priority = priority,
                                difficulty = difficulty,
                                examDate = examDate.trim(),
                                estimatedHours = estimatedHours.trim(),
                                weeklyGoalMinutes = weeklyGoal.trim(),
                                createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                                topics = initial?.topics.orEmpty()
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Salvar") }
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancelar") }
        }
    }
}

@Composable
private fun StudyTopicForm(subjectId: Long, initial: StudyTopicPlan?, onCancel: () -> Unit, onSave: (StudyTopicPlan) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var status by remember(initial?.id) { mutableStateOf(initial?.status ?: TopicStatus.NOT_STARTED) }
    var difficulty by remember(initial?.id) { mutableStateOf(initial?.difficulty ?: StudyDifficulty.MEDIUM) }
    var estimatedMinutes by remember(initial?.id) { mutableStateOf(initial?.estimatedMinutes.orEmpty()) }
    var note by remember(initial?.id) { mutableStateOf(initial?.note.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    PlanningFormCard(if (initial == null) "Novo tema" else "Editar tema") {
        OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Nome do tema") }, modifier = Modifier.fillMaxWidth())
        PlanningSelector("Status inicial", TopicStatus.entries, status, { it.label() }) { status = it }
        PlanningSelector("Dificuldade", StudyDifficulty.entries, difficulty, { it.label() }) { difficulty = it }
        OutlinedTextField(value = estimatedMinutes, onValueChange = { estimatedMinutes = it }, label = { Text("Tempo estimado em minutos (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = note, onValueChange = { note = it.take(140) }, label = { Text("Observação curta (opcional)") }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = LearnerCoral, fontWeight = FontWeight.Bold) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(shape = MaterialTheme.shapes.small,
                onClick = {
                    if (name.isBlank()) {
                        error = "Nome do tema é obrigatório para salvar."
                    } else {
                        onSave(
                            StudyTopicPlan(
                                id = initial?.id ?: System.currentTimeMillis(),
                                subjectId = subjectId,
                                name = name.trim(),
                                status = status,
                                difficulty = difficulty,
                                estimatedMinutes = estimatedMinutes.trim(),
                                note = note.trim(),
                                createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                                tasks = initial?.tasks.orEmpty()
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Salvar") }
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancelar") }
        }
    }
}

@Composable
private fun PlanningFormCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontFamily = CircularDisplayFamily, fontSize = 22.sp, fontWeight = FontWeight.Black)
            content()
        }
    }
}

@Composable
private fun <T> PlanningSelector(label: String, values: List<T>, selected: T, labelFor: (T) -> String, onSelected: (T) -> Unit) {
    Text(label, color = LearnerMutedText, fontWeight = FontWeight.Bold)
    values.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { value ->
                ReusablePlanningChip(labelFor(value), selected == value, Modifier.weight(1f)) { onSelected(value) }
            }
        }
    }
}

@Composable
private fun ReusablePlanningChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) LearnerPrimary else LearnerPrimaryTint).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else LearnerPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PlanningPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(99.dp)).background(color.copy(alpha = .13f)).padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyPlanningCard(title: String, text: String, action: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(LearnerPrimaryTint).padding(horizontal = 22.dp, vertical = 16.dp)) {
                Text("PLANO", color = LearnerPrimary, fontWeight = FontWeight.Black)
            }
            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, textAlign = TextAlign.Center)
            Text(text, color = LearnerMutedText, textAlign = TextAlign.Center)
            Button(shape = MaterialTheme.shapes.small, onClick = onAction) { Text(action) }
        }
    }
}

private fun SubjectPriority.label() = when (this) {
    SubjectPriority.LOW -> "Baixa"
    SubjectPriority.MEDIUM -> "Média"
    SubjectPriority.HIGH -> "Alta"
}

private fun StudyDifficulty.label() = when (this) {
    StudyDifficulty.EASY -> "Fácil"
    StudyDifficulty.MEDIUM -> "Média"
    StudyDifficulty.HARD -> "Difícil"
}

private fun TopicStatus.label() = when (this) {
    TopicStatus.NOT_STARTED -> "Não iniciado"
    TopicStatus.IN_PROGRESS -> "Em andamento"
    TopicStatus.STUDIED -> "Estudado"
    TopicStatus.NEEDS_REVIEW -> "Precisa revisar"
    TopicStatus.MASTERED -> "Dominado"
}

fun loadStudyPlanningSubjects(context: Context, userId: Long): List<StudySubjectPlan> {
    val raw = context.getSharedPreferences("study_planning", Context.MODE_PRIVATE).getString("subjects_$userId", null)
        ?: return if (BuildConfig.DEMO_MODE || userId == 1001L) demoStudyPlanningSubjects() else emptyList()
    return runCatching {
        val array = JSONArray(raw)
        List(array.length()) { index -> array.getJSONObject(index).toStudySubjectPlan() }
    }.getOrDefault(if (BuildConfig.DEMO_MODE || userId == 1001L) demoStudyPlanningSubjects() else emptyList())
}

private fun saveStudyPlanningSubjects(context: Context, userId: Long, subjects: List<StudySubjectPlan>) {
    val array = JSONArray()
    subjects.forEach { array.put(it.toJson()) }
    context.getSharedPreferences("study_planning", Context.MODE_PRIVATE).edit().putString("subjects_$userId", array.toString()).apply()
}

fun ensureStudyPlanningSubject(context: Context, userId: Long, subjectName: String): Long? {
    val cleanName = subjectName.trim()
    if (cleanName.isBlank()) return null
    val subjects = loadStudyPlanningSubjects(context, userId)
    val existing = subjects.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
    if (existing != null) return existing.id

    val now = System.currentTimeMillis()
    val created = StudySubjectPlan(
        id = now,
        name = cleanName,
        priority = SubjectPriority.MEDIUM,
        difficulty = StudyDifficulty.MEDIUM,
        examDate = "",
        estimatedHours = "",
        weeklyGoalMinutes = "",
        createdAt = now,
        topics = emptyList()
    )
    saveStudyPlanningSubjects(context, userId, subjects + created)
    return created.id
}

fun ensureStudyPlanningTopic(context: Context, userId: Long, subjectName: String, topicName: String): Long? {
    val cleanSubject = subjectName.trim()
    val cleanTopic = topicName.trim()
    if (cleanSubject.isBlank() || cleanTopic.isBlank()) return null

    val subjects = loadStudyPlanningSubjects(context, userId)
    val subject = subjects.firstOrNull { it.name.equals(cleanSubject, ignoreCase = true) }
    val subjectId = subject?.id ?: ensureStudyPlanningSubject(context, userId, cleanSubject) ?: return null
    val refreshedSubjects = loadStudyPlanningSubjects(context, userId)
    val next = refreshedSubjects.map { current ->
        if (current.id != subjectId) return@map current
        if (current.topics.any { it.name.equals(cleanTopic, ignoreCase = true) }) return@map current
        val now = System.currentTimeMillis()
        current.copy(
            topics = current.topics + StudyTopicPlan(
                id = now,
                subjectId = subjectId,
                name = cleanTopic,
                status = TopicStatus.NOT_STARTED,
                difficulty = null,
                estimatedMinutes = "",
                note = "",
                createdAt = now,
                tasks = emptyList()
            )
        )
    }
    saveStudyPlanningSubjects(context, userId, next)
    return subjectId
}

private fun StudySubjectPlan.toJson() = JSONObject()
    .put("id", id)
    .put("name", name)
    .put("priority", priority.name)
    .put("difficulty", difficulty.name)
    .put("examDate", examDate)
    .put("estimatedHours", estimatedHours)
    .put("weeklyGoalMinutes", weeklyGoalMinutes)
    .put("createdAt", createdAt)
    .put("topics", JSONArray().also { array -> topics.forEach { array.put(it.toJson()) } })

private fun StudyTopicPlan.toJson() = JSONObject()
    .put("id", id)
    .put("subjectId", subjectId)
    .put("name", name)
    .put("status", status.name)
    .put("difficulty", difficulty?.name ?: "")
    .put("estimatedMinutes", estimatedMinutes)
    .put("note", note)
    .put("createdAt", createdAt)
    .put("tasks", JSONArray().also { array -> tasks.forEach { array.put(it.toJson()) } })

private fun StudyTaskPlan.toJson() = JSONObject()
    .put("id", id)
    .put("title", title)
    .put("studyType", studyType)
    .put("status", status.name)
    .put("orderIndex", orderIndex)
    .put("completedAt", completedAt)

private fun JSONObject.toStudySubjectPlan(): StudySubjectPlan {
    val topicsArray = optJSONArray("topics") ?: JSONArray()
    return StudySubjectPlan(
        id = optLong("id"),
        name = optString("name"),
        priority = enumValueOrDefault(optString("priority"), SubjectPriority.MEDIUM),
        difficulty = enumValueOrDefault(optString("difficulty"), StudyDifficulty.MEDIUM),
        examDate = optString("examDate"),
        estimatedHours = optString("estimatedHours"),
        weeklyGoalMinutes = optString("weeklyGoalMinutes"),
        createdAt = optLong("createdAt"),
        topics = List(topicsArray.length()) { index -> topicsArray.getJSONObject(index).toStudyTopicPlan() }
    )
}

private fun JSONObject.toStudyTopicPlan(): StudyTopicPlan {
    val difficultyName = optString("difficulty")
    val tasksArray = optJSONArray("tasks")
    return StudyTopicPlan(
        id = optLong("id"),
        subjectId = optLong("subjectId"),
        name = optString("name"),
        status = enumValueOrDefault(optString("status"), TopicStatus.NOT_STARTED),
        difficulty = if (difficultyName.isBlank()) null else enumValueOrDefault(difficultyName, StudyDifficulty.MEDIUM),
        estimatedMinutes = optString("estimatedMinutes"),
        note = optString("note"),
        createdAt = optLong("createdAt"),
        tasks = if (tasksArray == null) defaultStudyTasks() else List(tasksArray.length()) { index -> tasksArray.getJSONObject(index).toStudyTaskPlan() }
    )
}

private fun JSONObject.toStudyTaskPlan(): StudyTaskPlan =
    StudyTaskPlan(
        id = optLong("id"),
        title = optString("title"),
        studyType = optString("studyType", "Leitura"),
        status = enumValueOrDefault(optString("status"), StudyTaskStatus.PENDING),
        orderIndex = optInt("orderIndex"),
        completedAt = if (has("completedAt") && !isNull("completedAt")) optLong("completedAt") else null
    )

fun markStudyTaskCompleted(context: Context, userId: Long, subjectId: Long, topicId: Long, taskId: Long) {
    if (subjectId == 0L || topicId == 0L || taskId == 0L) return
    val updated = loadStudyPlanningSubjects(context, userId).map { subject ->
        if (subject.id != subjectId) subject else subject.copy(
            topics = subject.topics.map { topic ->
                if (topic.id != topicId) topic else topic.copy(
                    tasks = topic.tasks.map { task ->
                        if (task.id == taskId) task.copy(status = StudyTaskStatus.COMPLETED, completedAt = System.currentTimeMillis()) else task
                    }
                )
            }
        )
    }
    saveStudyPlanningSubjects(context, userId, updated)
}

private fun defaultStudyTasks(): List<StudyTaskPlan> =
    listOf(
        "Ler ou assistir o conteúdo" to "Leitura",
        "Fazer exercícios" to "Exercício",
        "Corrigir erros" to "Exercício",
        "Anotar dúvidas" to "Resumo",
        "Fazer revisão rápida" to "Revisão"
    ).mapIndexed { index, item ->
        StudyTaskPlan(
            id = System.currentTimeMillis() + index,
            title = item.first,
            studyType = item.second,
            orderIndex = index
        )
    }

private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T): T =
    runCatching { enumValueOf<T>(name) }.getOrDefault(default)
