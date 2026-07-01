package com.frontend.educationalapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.GeneratedQuizResponse
import com.frontend.educationalapplication.ui.theme.CircularDisplayFamily
import com.frontend.educationalapplication.ui.theme.LearnerCoral
import com.frontend.educationalapplication.ui.theme.LearnerMutedText
import com.frontend.educationalapplication.ui.theme.LearnerPrimary
import com.frontend.educationalapplication.ui.theme.LearnerPrimaryTint
import kotlinx.coroutines.launch

@Composable
fun AiQuizGeneratorScreen(
    api: EducationalApi,
    onBack: () -> Unit
) {
    var subject by remember { mutableStateOf("Matematica") }
    var questionCount by remember { mutableIntStateOf(5) }
    var sourceText by remember {
        mutableStateOf(
            "Cole aqui ate umas duas paginas de conteudo. Exemplo: resumo da aula, texto do PDF, anotacoes ou uma explicacao que voce quer transformar em perguntas."
        )
    }
    var result by remember { mutableStateOf<GeneratedQuizResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Gerar quiz com IA", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(
            "Cole um texto e o Gemini gera perguntas universais para usar depois nos jogos.",
            color = LearnerMutedText
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LearnerPrimaryTint)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(LearnerPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", color = Color.White, fontWeight = FontWeight.Black)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Teste da API Gemini", fontWeight = FontWeight.Black)
                    Text("A chave fica no backend via GEMINI_API_KEY.", color = LearnerMutedText, fontSize = 13.sp)
                }
            }
        }

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tema ou materia") },
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(3, 5, 8).forEach { amount ->
                GameModeChip(
                    text = "$amount perguntas",
                    selected = questionCount == amount,
                    onClick = { questionCount = amount },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        OutlinedTextField(
            value = sourceText,
            onValueChange = {
                sourceText = it.take(9000)
                error = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            label = { Text("Texto base") },
            supportingText = { Text("${sourceText.length}/9000 caracteres") }
        )

        Button(
            onClick = {
                if (loading) return@Button
                loading = true
                error = null
                result = null
                scope.launch {
                    runCatching {
                        api.generateQuizFromText(
                            text = sourceText,
                            subject = subject,
                            gameMode = "Universal",
                            questionCount = questionCount
                        )
                    }.onSuccess {
                        result = it
                    }.onFailure {
                        error = it.toFriendlyError().description
                    }
                    loading = false
                }
            },
            enabled = sourceText.trim().length >= 120 && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Gerando..." else "Gerar perguntas")
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(99.dp)),
                color = LearnerPrimary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }

        error?.let {
            ErrorStateCard(error = it.toFriendlyError())
        }

        result?.let { quiz ->
            AiQuizResult(
                api = api,
                quiz = quiz,
                onIgnored = { result = null }
            )
        }

        BottomBackButton(onBack = onBack)
    }
}

@Composable
private fun GameModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) LearnerPrimary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else LearnerMutedText,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun AiQuizResult(api: EducationalApi, quiz: GeneratedQuizResponse, onIgnored: () -> Unit) {
    val scope = rememberCoroutineScope()
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var savedIndexes by remember { mutableStateOf<Set<Int>>(emptySet()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Perguntas geradas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("${quiz.questions.size} perguntas universais", color = LearnerMutedText)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    scope.launch {
                        runCatching { api.saveGeneratedQuestions(quiz.source, "Universal", quiz.questions) }
                            .onSuccess {
                                savedIndexes = quiz.questions.indices.toSet()
                                saveMessage = "Todas as perguntas foram salvas."
                            }
                            .onFailure { saveMessage = it.toFriendlyError().description }
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Salvar todas") }
            Button(
                onClick = onIgnored,
                modifier = Modifier.weight(1f)
            ) { Text("Ignorar") }
        }
        saveMessage?.let { Text(it, color = LearnerMutedText, fontWeight = FontWeight.Bold) }

        quiz.questions.forEachIndexed { index, question ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${index + 1}. ${question.question}", fontWeight = FontWeight.Black)
                    question.options.forEachIndexed { optionIndex, option ->
                        val isCorrect = optionIndex == question.correctIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCorrect) LearnerPrimary.copy(alpha = .12f) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${'A' + optionIndex}",
                                color = if (isCorrect) LearnerPrimary else LearnerMutedText,
                                fontWeight = FontWeight.Black
                            )
                            Text(option, modifier = Modifier.weight(1f))
                        }
                    }
                    if (question.explanation.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text("Explicacao: ${question.explanation}", color = LearnerMutedText, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                runCatching { api.saveGeneratedQuestions(quiz.source, "Universal", listOf(question)) }
                                    .onSuccess {
                                        savedIndexes = savedIndexes + index
                                        saveMessage = "Pergunta ${index + 1} salva."
                                    }
                                    .onFailure { saveMessage = it.toFriendlyError().description }
                            }
                        },
                        enabled = index !in savedIndexes,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (index in savedIndexes) "Salva" else "Salvar esta")
                    }
                }
            }
        }
        Text(
            "Voce pode salvar todas, salvar algumas ou ignorar. As perguntas salvas ficam universais no banco para usar depois nos jogos.",
            color = LearnerCoral,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
