package com.frontend.educationalapplication

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.PlannedRevisionResponse
import com.frontend.educationalapplication.network.RevisionRoadmapResponse
import com.frontend.educationalapplication.network.StudyPlanOptionResponse
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import com.frontend.educationalapplication.ui.theme.LearnerTeal
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private enum class SmartPlanStep { CREATE, CHOOSE, REVIEW, SAVED }

@Composable
fun SmartRevisionRoadmapScreen(
    api: EducationalApi,
    userId: Long,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(SmartPlanStep.CREATE) }
    var base by remember { mutableStateOf(SubjectBases.HIGH_SCHOOL) }
    var subject by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var planWholeSubject by remember { mutableStateOf(true) }
    var durationDays by remember { mutableIntStateOf(7) }
    var dailyMinutes by remember { mutableIntStateOf(30) }
    var preferredTime by remember { mutableStateOf("19:00") }
    var level by remember { mutableStateOf("Iniciante") }
    var goal by remember { mutableStateOf("Prova") }
    var roadmap by remember { mutableStateOf<RevisionRoadmapResponse?>(null) }
    var selectedOption by remember { mutableStateOf<StudyPlanOptionResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val editableRevisions = remember { mutableStateListOf<EditablePlannedRevision>() }

    when (step) {
        SmartPlanStep.CREATE -> SmartPlanCreateStep(
            context = context,
            userId = userId,
            base = base,
            onBase = { base = it; subject = ""; topic = ""; planWholeSubject = true },
            subject = subject,
            onSubject = { subject = it; topic = ""; planWholeSubject = true },
            topic = topic,
            onTopic = { topic = it; planWholeSubject = it.isBlank() },
            planWholeSubject = planWholeSubject,
            onPlanWholeSubject = { planWholeSubject = it; if (it) topic = "" },
            durationDays = durationDays,
            onDurationDays = { durationDays = it },
            dailyMinutes = dailyMinutes,
            onDailyMinutes = { dailyMinutes = it },
            preferredTime = preferredTime,
            onPreferredTime = { preferredTime = it },
            level = level,
            onLevel = { level = it },
            goal = goal,
            onGoal = { goal = it },
            loading = loading,
            message = message,
            onGenerate = {
                if (subject.isBlank()) {
                    message = "Escolha uma matéria para gerar o planejamento."
                    return@SmartPlanCreateStep
                }
                loading = true
                message = null
                scope.launch {
                    runCatching {
                        api.generateRevisionRoadmap(
                            baseType = base,
                            subjectName = subject,
                            topicName = topic,
                            planWholeSubject = planWholeSubject,
                            durationDays = durationDays,
                            dailyAvailableMinutes = dailyMinutes,
                            preferredTime = preferredTime,
                            level = level,
                            goal = goal
                        )
                    }.onSuccess {
                        roadmap = it
                        step = SmartPlanStep.CHOOSE
                    }.onFailure {
                        message = it.toFriendlyError().description
                    }
                    loading = false
                }
            }
        )

        SmartPlanStep.CHOOSE -> SmartPlanChooseStep(
            roadmap = roadmap,
            onChoose = { option ->
                selectedOption = option
                editableRevisions.clear()
                editableRevisions.addAll(option.revisions.map { it.toEditable() })
                step = SmartPlanStep.REVIEW
            },
            onBack = { step = SmartPlanStep.CREATE }
        )

        SmartPlanStep.REVIEW -> SmartPlanReviewStep(
            option = selectedOption,
            revisions = editableRevisions,
            message = message,
            onBack = { step = SmartPlanStep.CHOOSE },
            onSave = {
                val option = selectedOption ?: return@SmartPlanReviewStep
                loading = true
                message = null
                scope.launch {
                    runCatching {
                        saveSmartPlan(
                            context = context,
                            api = api,
                            userId = userId,
                            base = base,
                            subject = subject,
                            topic = topic,
                            option = option,
                            revisions = editableRevisions.toList(),
                            durationDays = durationDays,
                            dailyMinutes = dailyMinutes,
                            preferredTime = preferredTime,
                            level = level,
                            goal = goal
                        )
                    }.onSuccess {
                        onSaved()
                        step = SmartPlanStep.SAVED
                    }.onFailure {
                        message = it.toFriendlyError().description
                    }
                    loading = false
                }
            }
        )

        SmartPlanStep.SAVED -> SectionCard("Plano salvo com sucesso") {
            Text("As revisões foram programadas e os temas sugeridos já entram no menu do Relatório.", color = LearnerMutedText)
            Button(onClick = {
                roadmap = null
                selectedOption = null
                editableRevisions.clear()
                step = SmartPlanStep.CREATE
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Criar outro plano")
            }
        }
    }

    if (loading && step != SmartPlanStep.CREATE) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SmartPlanCreateStep(
    context: Context,
    userId: Long,
    base: String,
    onBase: (String) -> Unit,
    subject: String,
    onSubject: (String) -> Unit,
    topic: String,
    onTopic: (String) -> Unit,
    planWholeSubject: Boolean,
    onPlanWholeSubject: (Boolean) -> Unit,
    durationDays: Int,
    onDurationDays: (Int) -> Unit,
    dailyMinutes: Int,
    onDailyMinutes: (Int) -> Unit,
    preferredTime: String,
    onPreferredTime: (String) -> Unit,
    level: String,
    onLevel: (String) -> Unit,
    goal: String,
    onGoal: (String) -> Unit,
    loading: Boolean,
    message: String?,
    onGenerate: () -> Unit
) {
    SectionCard("Criar plano de revisão") {
        Text("Escolha o essencial. A IA gera 2 opções e você revisa tudo antes de salvar.", color = LearnerMutedText)
        SmartChipRow(listOf(SubjectBases.HIGH_SCHOOL, SubjectBases.COMPUTING, SubjectBases.CUSTOM), base, onBase)
        Text("Matéria", fontWeight = FontWeight.Black)
        val subjects = remember(base, userId) { searchSubjects(context, base, "", userId).take(8) }
        subjects.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { item -> SmartChip(item.name, subject == item.name, Modifier.weight(1f)) { onSubject(item.name) } }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
        OutlinedTextField(value = subject, onValueChange = onSubject, label = { Text("Ou criar matéria") }, modifier = Modifier.fillMaxWidth())

        if (subject.isNotBlank()) {
            Text("Tema", fontWeight = FontWeight.Black)
            SmartChip("Planejar matéria inteira", planWholeSubject, Modifier.fillMaxWidth()) { onPlanWholeSubject(true) }
            searchTopics(context, subject, "", userId).take(6).chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { item -> SmartChip(item, topic == item && !planWholeSubject, Modifier.weight(1f)) { onTopic(item) } }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
            OutlinedTextField(value = topic, onValueChange = { onTopic(it) }, label = { Text("Ou criar novo tema") }, modifier = Modifier.fillMaxWidth())
        }

        Text("Prazo disponível", fontWeight = FontWeight.Black)
        SmartChipRow(listOf("1 dia", "3 dias", "1 semana", "2 semanas", "1 mês"), durationDays.toDurationLabel()) { onDurationDays(it.toDurationDays()) }
        Text("Tempo por dia", fontWeight = FontWeight.Black)
        SmartChipRow(listOf("15 min", "30 min", "45 min", "1 hora", "2 horas"), dailyMinutes.toDailyLabel()) { onDailyMinutes(it.toDailyMinutes()) }
        OutlinedTextField(value = preferredTime, onValueChange = onPreferredTime, label = { Text("Horário preferido") }, modifier = Modifier.fillMaxWidth())
        Text("Nível", fontWeight = FontWeight.Black)
        SmartChipRow(listOf("Iniciante", "Intermediário", "Avançado"), level, onLevel)
        Text("Objetivo", fontWeight = FontWeight.Black)
        SmartChipRow(listOf("Prova", "ENEM", "Faculdade", "Trabalho", "Estudo pessoal"), goal, onGoal)

        Button(enabled = !loading && subject.isNotBlank(), onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Gerando..." else "Gerar plano com IA")
        }
        message?.let { Text(it, color = LearnerCoral, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SmartPlanChooseStep(
    roadmap: RevisionRoadmapResponse?,
    onChoose: (StudyPlanOptionResponse) -> Unit,
    onBack: () -> Unit
) {
    SectionCard("Escolha seu plano") {
        roadmap?.options.orEmpty().forEach { option ->
            SmartPlanOptionCard(option = option, onChoose = { onChoose(option) })
        }
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
    }
}

@Composable
private fun SmartPlanOptionCard(option: StudyPlanOptionResponse, onChoose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Opção ${option.optionNumber}: ${option.title}", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(option.description, color = LearnerMutedText)
            Text("${option.revisions.size} revisões • ${option.estimatedTotalMinutes} min • ${option.difficulty}", color = LearnerTeal, fontWeight = FontWeight.Bold)
            option.revisions.take(3).forEach {
                Text("• ${it.date} ${it.time} — ${it.topicName}", color = LearnerMutedText, fontSize = 12.sp)
            }
            Button(onClick = onChoose, modifier = Modifier.fillMaxWidth()) { Text("Escolher esta opção") }
        }
    }
}

@Composable
private fun SmartPlanReviewStep(
    option: StudyPlanOptionResponse?,
    revisions: MutableList<EditablePlannedRevision>,
    message: String?,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    SectionCard("Revisar plano") {
        Text(option?.title.orEmpty(), fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text("Edite manualmente antes de salvar. A IA não altera mais nada nesta etapa.", color = LearnerMutedText)
        revisions.forEachIndexed { index, revision ->
            EditableRevisionCard(
                revision = revision,
                onRemove = { revisions.removeAt(index) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Voltar") }
            Button(enabled = revisions.isNotEmpty(), onClick = onSave, modifier = Modifier.weight(1f)) { Text("Salvar plano") }
        }
        message?.let { Text(it, color = LearnerCoral, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun EditableRevisionCard(revision: EditablePlannedRevision, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LearnerPrimaryTint.copy(alpha = .45f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${revision.subjectName} - ${revision.topicName}", fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = revision.date, onValueChange = { revision.date = it }, label = { Text("Data") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = revision.time, onValueChange = { revision.time = it }, label = { Text("Hora") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = revision.topicName, onValueChange = { revision.topicName = it }, label = { Text("Tema") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = revision.durationMinutes, onValueChange = { revision.durationMinutes = it.filter(Char::isDigit).take(3) }, label = { Text("Min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                OutlinedTextField(value = revision.priority, onValueChange = { revision.priority = it }, label = { Text("Prioridade") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = revision.revisionType, onValueChange = { revision.revisionType = it }, label = { Text("Tipo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = revision.description, onValueChange = { revision.description = it }, label = { Text("Descrição curta") }, modifier = Modifier.fillMaxWidth())
            TextButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) { Text("Remover revisão", color = LearnerCoral) }
        }
    }
}

@Composable
private fun SmartChipRow(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    options.chunked(3).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { item -> SmartChip(item, selected == item, Modifier.weight(1f)) { onSelected(item) } }
            repeat(3 - row.size) { Box(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun SmartChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) LearnerPrimary else LearnerPrimaryTint)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else LearnerPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

private class EditablePlannedRevision(
    subjectName: String,
    topicName: String,
    date: String,
    time: String,
    durationMinutes: String,
    revisionType: String,
    description: String,
    priority: String
) {
    var subjectName by mutableStateOf(subjectName)
    var topicName by mutableStateOf(topicName)
    var date by mutableStateOf(date)
    var time by mutableStateOf(time)
    var durationMinutes by mutableStateOf(durationMinutes)
    var revisionType by mutableStateOf(revisionType)
    var description by mutableStateOf(description)
    var priority by mutableStateOf(priority)
}

private fun PlannedRevisionResponse.toEditable() = EditablePlannedRevision(
    subjectName = subjectName,
    topicName = topicName,
    date = date,
    time = time,
    durationMinutes = durationMinutes.toString(),
    revisionType = revisionType,
    description = description,
    priority = priority
)

private suspend fun saveSmartPlan(
    context: Context,
    api: EducationalApi,
    userId: Long,
    base: String,
    subject: String,
    topic: String,
    option: StudyPlanOptionResponse,
    revisions: List<EditablePlannedRevision>,
    durationDays: Int,
    dailyMinutes: Int,
    preferredTime: String,
    level: String,
    goal: String
) {
    option.suggestedTopics.forEach { suggested ->
        if (suggested.isNotBlank()) addCustomTopicToPlanning(context, userId, subject, suggested)
    }
    revisions.forEach { revision ->
        addCustomTopicToPlanning(context, userId, revision.subjectName, revision.topicName)
        api.createReview(
            userId = userId,
            title = "Revisar ${revision.topicName.ifBlank { revision.subjectName }}",
            subject = revision.subjectName.ifBlank { subject },
            dueDate = revision.date,
            topic = revision.topicName,
            notes = "${revision.time} • ${revision.durationMinutes} min • ${revision.revisionType} • ${revision.priority} • ${revision.description}",
            originalDifficulty = option.difficulty
        )
    }
    saveSmartPlanHistory(context, userId, base, subject, topic, option, revisions, durationDays, dailyMinutes, preferredTime, level, goal)
}

private fun saveSmartPlanHistory(
    context: Context,
    userId: Long,
    base: String,
    subject: String,
    topic: String,
    option: StudyPlanOptionResponse,
    revisions: List<EditablePlannedRevision>,
    durationDays: Int,
    dailyMinutes: Int,
    preferredTime: String,
    level: String,
    goal: String
) {
    val prefs = context.getSharedPreferences("smart_revision_plans", Context.MODE_PRIVATE)
    val key = "plans_$userId"
    val array = JSONArray(prefs.getString(key, "[]"))
    array.put(
        JSONObject()
            .put("id", System.currentTimeMillis())
            .put("title", option.title)
            .put("subjectName", subject)
            .put("baseType", base)
            .put("selectedTopic", topic)
            .put("durationDays", durationDays)
            .put("dailyAvailableMinutes", dailyMinutes)
            .put("preferredTime", preferredTime)
            .put("level", level)
            .put("goal", goal)
            .put("selectedOption", option.optionNumber)
            .put("createdAt", System.currentTimeMillis())
            .put("revisions", JSONArray(revisions.map {
                JSONObject()
                    .put("subjectName", it.subjectName)
                    .put("topicName", it.topicName)
                    .put("date", it.date)
                    .put("time", it.time)
                    .put("durationMinutes", it.durationMinutes)
                    .put("revisionType", it.revisionType)
                    .put("description", it.description)
                    .put("priority", it.priority)
            }))
    )
    prefs.edit().putString(key, array.toString()).apply()
}

private fun Int.toDurationLabel(): String = when (this) {
    1 -> "1 dia"
    3 -> "3 dias"
    14 -> "2 semanas"
    30 -> "1 mês"
    else -> "1 semana"
}

private fun String.toDurationDays(): Int = when (this) {
    "1 dia" -> 1
    "3 dias" -> 3
    "2 semanas" -> 14
    "1 mês" -> 30
    else -> 7
}

private fun Int.toDailyLabel(): String = when (this) {
    15 -> "15 min"
    45 -> "45 min"
    60 -> "1 hora"
    120 -> "2 horas"
    else -> "30 min"
}

private fun String.toDailyMinutes(): Int = when (this) {
    "15 min" -> 15
    "45 min" -> 45
    "1 hora" -> 60
    "2 horas" -> 120
    else -> 30
}
