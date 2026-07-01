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
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import kotlinx.coroutines.launch

@Composable
fun QuestionBankScreen(api: EducationalApi, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<SavedQuestionResponse>>(emptyList()) }
    var lists by remember { mutableStateOf<List<QuestionListResponse>>(emptyList()) }
    var newListName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        loading = true
        message = null
        scope.launch {
            runCatching { api.savedQuestions(query) to api.questionLists() }
                .onSuccess {
                    questions = it.first
                    lists = it.second
                }
                .onFailure { message = it.toFriendlyError().description }
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Banco de perguntas", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Busque, ajuste o tema, crie listas e organize perguntas do seu jeito.", color = LearnerMutedText)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Minhas listas", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Crie listas como Difíceis, Prova de sexta ou Para jogar com amigos.", color = LearnerMutedText, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nova lista") },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (newListName.isBlank()) return@Button
                            scope.launch {
                                runCatching { api.createQuestionList(newListName.trim()) }
                                    .onSuccess {
                                        lists = listOf(it) + lists
                                        newListName = ""
                                        message = "Lista criada."
                                    }
                                    .onFailure { message = it.toFriendlyError().description }
                            }
                        }
                    ) { Text("Criar") }
                }
                if (lists.isEmpty()) {
                    Text("Nenhuma lista criada ainda.", color = LearnerMutedText)
                } else {
                    lists.forEach { list ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${list.name} • ${list.questionCount}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    scope.launch {
                                        runCatching { api.deleteQuestionList(list.id) }
                                            .onSuccess {
                                                lists = lists.filterNot { it.id == list.id }
                                                message = "Lista eliminada."
                                            }
                                            .onFailure { message = it.toFriendlyError().description }
                                    }
                                }
                            ) { Text("Eliminar") }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                label = { Text("Buscar") },
                singleLine = true
            )
            Button(onClick = { load() }) { Text("Ir") }
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)),
                color = LearnerPrimary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
        message?.let { Text(it, color = LearnerMutedText, fontWeight = FontWeight.Bold) }

        if (!loading && questions.isEmpty()) {
            EmptyStateCard("Nenhuma pergunta salva", "Gere perguntas com IA e salve as que quiser manter.")
        }

        questions.forEach { question ->
            SavedQuestionCard(
                question = question,
                lists = lists,
                onToggleList = { list ->
                    scope.launch {
                        val isInList = question.id in list.questionIds
                        runCatching {
                            if (isInList) {
                                api.removeQuestionFromList(list.id, question.id)
                            } else {
                                api.addQuestionToList(list.id, question.id)
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
                                message = "Pergunta eliminada."
                            }
                            .onFailure { message = it.toFriendlyError().description }
                    }
                }
            )
        }

        BottomBackButton(onBack = onBack)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                Text("Explicacao: ${question.explanation}", color = LearnerMutedText, fontSize = 13.sp)
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
                Button(onClick = { onUpdate(subject) }, modifier = Modifier.weight(1f)) {
                    Text("Salvar")
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f)) {
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
            .clip(RoundedCornerShape(14.dp))
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
