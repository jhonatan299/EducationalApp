package com.frontend.educationalapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.QuestionListResponse
import com.frontend.educationalapplication.network.SavedQuestionResponse
import com.frontend.educationalapplication.ui.theme.CircularDisplayFamily
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import kotlinx.coroutines.launch
import com.frontend.educationalapplication.ui.theme.LearnerDeepIndigo

@Composable
fun QuestionBankScreen(api: EducationalApi, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<SavedQuestionResponse>>(emptyList()) }
    var lists by remember { mutableStateOf<List<QuestionListResponse>>(emptyList()) }
    var selectedListId by remember { mutableStateOf<Long?>(null) }
    var showingQuestions by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun selectedList(): QuestionListResponse? = lists.firstOrNull { it.id == selectedListId }

    fun load() {
        loading = true
        message = null
        scope.launch {
            runCatching { api.savedQuestions("") to api.questionLists() }
                .onSuccess {
                    questions = it.first
                    lists = it.second
                }
                .onFailure { message = it.toFriendlyError().description }
            loading = false
        }
    }

    fun openList(listId: Long?) {
        selectedListId = listId
        query = ""
        showingQuestions = true
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Banco de perguntas", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)

        if (!showingQuestions) {
            Text("Organize suas perguntas por listas e abra só o que precisa revisar.", color = LearnerMutedText)
            QuestionListHub(
                questions = questions,
                lists = lists,
                newListName = newListName,
                onNewListName = { newListName = it },
                onCreateList = {
                    if (newListName.isBlank()) return@QuestionListHub
                    scope.launch {
                        runCatching { api.createQuestionList(newListName.trim()) }
                            .onSuccess {
                                lists = listOf(it) + lists
                                newListName = ""
                                message = "Lista criada."
                            }
                            .onFailure { message = it.toFriendlyError().description }
                    }
                },
                onOpenAll = { openList(null) },
                onOpenList = { openList(it.id) },
                onDeleteList = { list ->
                    scope.launch {
                        runCatching { api.deleteQuestionList(list.id) }
                            .onSuccess {
                                lists = lists.filterNot { it.id == list.id }
                                message = "Lista eliminada."
                            }
                            .onFailure { message = it.toFriendlyError().description }
                    }
                }
            )
        } else {
            val list = selectedList()
            val visibleQuestions = remember(questions, lists, selectedListId, query) {
                val base = if (selectedListId == null) {
                    questions
                } else {
                    val ids = list?.questionIds.orEmpty().toSet()
                    questions.filter { it.id in ids }
                }
                base.filter { it.matchesQuestionQuery(query) }
            }

            QuestionListHeader(
                title = list?.name ?: "Todas as perguntas",
                subtitle = "${visibleQuestions.size} perguntas",
                onBack = {
                    showingQuestions = false
                    selectedListId = null
                    query = ""
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Buscar nesta lista") },
                    singleLine = true
                )
                Button(shape = MaterialTheme.shapes.small, onClick = { load() }) { Text("Atualizar") }
            }

            if (!loading && visibleQuestions.isEmpty()) {
                EmptyStateCard(
                    title = if (query.isBlank()) "Lista vazia" else "Nada encontrado",
                    subtitle = if (selectedListId == null) {
                        "Gere perguntas e salve as que quiser manter."
                    } else {
                        "Adicione perguntas a esta lista pela classificação dos cards."
                    }
                )
            }

            visibleQuestions.forEach { question ->
                SavedQuestionCard(
                    question = question,
                    lists = lists,
                    onToggleList = { questionList ->
                        scope.launch {
                            val isInList = question.id in questionList.questionIds
                            runCatching {
                                if (isInList) {
                                    api.removeQuestionFromList(questionList.id, question.id)
                                } else {
                                    api.addQuestionToList(questionList.id, question.id)
                                }
                            }.onSuccess { updated ->
                                lists = lists.map { old -> if (old.id == updated.id) updated else old }
                                message = if (isInList) "Pergunta removida da lista." else "Pergunta adicionada à lista."
                            }.onFailure { message = it.toFriendlyError().description }
                        }
                    },
                    onUpdate = { subject ->
                        scope.launch {
                            runCatching { api.updateSavedQuestion(question.id, subject) }
                                .onSuccess {
                                    questions = questions.map { old -> if (old.id == it.id) it else old }
                                    message = "Pergunta atualizada."
                                }
                                .onFailure { message = it.toFriendlyError().description }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            runCatching { api.deleteSavedQuestion(question.id) }
                                .onSuccess {
                                    questions = questions.filterNot { it.id == question.id }
                                    lists = lists.map { old ->
                                        if (question.id in old.questionIds) {
                                            val ids = old.questionIds - question.id
                                            old.copy(questionIds = ids, questionCount = ids.size.toLong())
                                        } else old
                                    }
                                    message = "Pergunta eliminada."
                                }
                                .onFailure { message = it.toFriendlyError().description }
                        }
                    }
                )
            }
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)),
                color = LearnerPrimary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
        message?.let { Text(it, color = LearnerMutedText, fontWeight = FontWeight.Bold) }

        BottomBackButton(onBack = if (showingQuestions) {
            {
                showingQuestions = false
                selectedListId = null
                query = ""
            }
        } else onBack)
    }
}

@Composable
private fun QuestionListHub(
    questions: List<SavedQuestionResponse>,
    lists: List<QuestionListResponse>,
    newListName: String,
    onNewListName: (String) -> Unit,
    onCreateList: () -> Unit,
    onOpenAll: () -> Unit,
    onOpenList: (QuestionListResponse) -> Unit,
    onDeleteList: (QuestionListResponse) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Listas", fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Separe perguntas por dificuldade, prova, tema ou jogo.", color = LearnerMutedText, fontSize = 13.sp)

            QuestionListCard(
                title = "Todas as perguntas",
                subtitle = "Busca geral do banco",
                count = questions.size.toLong(),
                accent = LearnerPrimary,
                onClick = onOpenAll
            )

            if (lists.isEmpty()) {
                EmptyStateCard("Nenhuma lista criada", "Crie uma lista para reduzir a bagunça visual.")
            } else {
                lists.forEach { list ->
                    QuestionListCard(
                        title = list.name,
                        subtitle = "Lista personalizada",
                        count = list.questionCount,
                        accent = LearnerDeepIndigo,
                        onClick = { onOpenList(list) },
                        onDelete = { onDeleteList(list) }
                    )
                }
            }

            Text("Criar lista", color = LearnerMutedText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = onNewListName,
                    modifier = Modifier.weight(1f),
                    label = { Text("Nome da lista") },
                    singleLine = true
                )
                Button(shape = MaterialTheme.shapes.small, onClick = onCreateList) { Text("Criar") }
            }
        }
    }
}

@Composable
private fun QuestionListCard(
    title: String,
    subtitle: String,
    count: Long,
    accent: Color,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.10f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$count", color = Color.White, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text(subtitle, color = LearnerMutedText, fontSize = 12.sp)
            }
            onDelete?.let {
                TextButton(onClick = it) { Text("Eliminar", color = LearnerCoral) }
            } ?: Text("Abrir", color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuestionListHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = LearnerPrimary)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.78f), fontSize = 13.sp)
            }
            Button(shape = MaterialTheme.shapes.small, onClick = onBack) { Text("Listas") }
        }
    }
}

@Composable
private fun SavedQuestionCard(
    question: SavedQuestionResponse,
    lists: List<QuestionListResponse>,
    onToggleList: (QuestionListResponse) -> Unit,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var subject by remember(question.id) { mutableStateOf(question.subject) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(question.question, fontWeight = FontWeight.Black, fontSize = 16.sp)
            question.options.forEachIndexed { index, option ->
                Text(
                    "${'A' + index}. $option",
                    color = if (index == question.correctIndex) LearnerPrimary else LearnerMutedText,
                    fontWeight = if (index == question.correctIndex) FontWeight.Bold else FontWeight.Normal
                )
            }
            if (question.explanation.isNotBlank()) {
                Text("Explicação: ${question.explanation}", color = LearnerMutedText, fontSize = 13.sp)
            }
            if (lists.isNotEmpty()) {
                Text("Classificar em listas", color = LearnerMutedText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                lists.forEach { list ->
                    val selected = question.id in list.questionIds
                    QuestionListChip(
                        text = if (selected) "✓ ${list.name}" else "+ ${list.name}",
                        selected = selected,
                        onClick = { onToggleList(list) }
                    )
                }
            }
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Tema") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(shape = MaterialTheme.shapes.small, onClick = { onUpdate(subject) }, modifier = Modifier.weight(1f)) {
                    Text("Salvar")
                }
                Button(shape = MaterialTheme.shapes.small, onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
private fun QuestionListChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) LearnerPrimary else LearnerPrimaryTint)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else LearnerPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun SavedQuestionResponse.matchesQuestionQuery(query: String): Boolean {
    val clean = query.trim()
    if (clean.isBlank()) return true
    return question.contains(clean, ignoreCase = true) ||
        subject.contains(clean, ignoreCase = true) ||
        explanation.contains(clean, ignoreCase = true) ||
        options.any { it.contains(clean, ignoreCase = true) }
}
