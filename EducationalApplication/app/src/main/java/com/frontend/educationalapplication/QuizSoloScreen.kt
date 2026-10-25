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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.frontend.educationalapplication.ui.theme.LearnerTeal
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import kotlin.math.roundToInt

enum class QuestionCategory { GENERAL, PROGRAMMING, DATABASE, MATH, HISTORY, SCIENCE, CUSTOM }

data class QuizQuestion(
    val id: Long,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val category: QuestionCategory
)

data class QuizSoloRoundHistory(
    val id: Long,
    val userId: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val earnedCoins: Int,
    val completedAt: String
)

private data class QuizQuestionSource(
    val id: Long?,
    val name: String,
    val questionCount: Int
)

private data class QuizSoloLoadResult(
    val sources: List<QuizQuestionSource>,
    val selectedSourceId: Long?,
    val selectedSourceName: String,
    val totalSavedQuestions: Int,
    val questions: List<QuizQuestion>
)

private enum class QuizSoloPhase { LOADING, EMPTY, ERROR, READY, QUESTION, FINISHED }

private data class QuizSoloUiState(
    val phase: QuizSoloPhase = QuizSoloPhase.LOADING,
    val sources: List<QuizQuestionSource> = emptyList(),
    val selectedSourceId: Long? = null,
    val selectedSourceName: String = "Todas as perguntas",
    val totalSavedQuestions: Int = 0,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val earnedCoins: Int = 0,
    val errorMessage: String? = null,
    val roundId: Long = System.currentTimeMillis()
)

object QuizSoloRewardRules {
    const val COINS_PER_CORRECT = 5
    const val PERFECT_ROUND_BONUS = 10

    fun calculate(correctAnswers: Int, totalQuestions: Int): Int {
        val base = correctAnswers * COINS_PER_CORRECT
        val bonus = if (totalQuestions > 0 && correctAnswers == totalQuestions) PERFECT_ROUND_BONUS else 0
        return base + bonus
    }
}

private class QuizQuestionRepository(private val api: EducationalApi) {
    suspend fun loadRoundQuestions(sourceId: Long?, count: Int): QuizSoloLoadResult {
        // Let a failure here propagate so the screen shows the error state instead of "no questions".
        val savedResponses = api.savedQuestions()
        val lists = runCatching { api.questionLists() }.getOrNull().orEmpty()
        val source = lists.firstOrNull { it.id == sourceId }
        val sourceIds = source?.questionIds?.toSet()
        val selectedResponses = if (sourceIds == null) {
            savedResponses
        } else {
            savedResponses.filter { it.id in sourceIds }
        }
        val questions = selectedResponses
            .mapNotNull { it.toQuizQuestionOrNull() }
            .shuffled()
            .take(count)
            .map { it.withShuffledOptions() }
        val sources = buildList {
            add(QuizQuestionSource(null, "Todas as perguntas", savedResponses.size))
            lists.forEach { list ->
                add(QuizQuestionSource(list.id, list.name, list.questionCount.toInt()))
            }
        }
        return QuizSoloLoadResult(
            sources = sources,
            selectedSourceId = source?.id,
            selectedSourceName = source?.name ?: "Todas as perguntas",
            totalSavedQuestions = savedResponses.size,
            questions = questions
        )
    }

    private fun SavedQuestionResponse.toQuizQuestionOrNull(): QuizQuestion? {
        if (options.size != 4 || correctIndex !in 0..3) return null
        return QuizQuestion(
            id = id,
            questionText = question,
            options = options,
            correctAnswerIndex = correctIndex,
            category = QuestionCategory.CUSTOM
        )
    }

    private fun QuizQuestion.withShuffledOptions(): QuizQuestion {
        val correctAnswer = options[correctAnswerIndex]
        val shuffled = options.shuffled()
        return copy(options = shuffled, correctAnswerIndex = shuffled.indexOf(correctAnswer))
    }
}

private class QuizSoloViewModel(
    private val repository: QuizQuestionRepository,
    private val userId: Long,
    private val context: Context
) {
    var state by mutableStateOf(QuizSoloUiState())
        private set

    suspend fun startQuiz(sourceId: Long? = state.selectedSourceId) {
        state = state.copy(phase = QuizSoloPhase.LOADING, selectedSourceId = sourceId)
        runCatching { repository.loadRoundQuestions(sourceId, ROUND_SIZE) }
            .onSuccess { result ->
                state = if (result.totalSavedQuestions == 0) {
                    QuizSoloUiState(
                        phase = QuizSoloPhase.EMPTY,
                        sources = result.sources,
                        selectedSourceId = result.selectedSourceId,
                        selectedSourceName = result.selectedSourceName,
                        totalSavedQuestions = result.totalSavedQuestions
                    )
                } else {
                    QuizSoloUiState(
                        phase = QuizSoloPhase.READY,
                        sources = result.sources,
                        selectedSourceId = result.selectedSourceId,
                        selectedSourceName = result.selectedSourceName,
                        totalSavedQuestions = result.totalSavedQuestions,
                        questions = result.questions
                    )
                }
            }
            .onFailure {
                state = QuizSoloUiState(
                    phase = QuizSoloPhase.ERROR,
                    errorMessage = "Não foi possível carregar o quiz. Tente novamente."
                )
            }
    }

    suspend fun chooseSource(sourceId: Long?) {
        startQuiz(sourceId)
    }

    fun beginRound() {
        if (state.questions.isEmpty()) return
        state = state.copy(
            phase = QuizSoloPhase.QUESTION,
            currentIndex = 0,
            selectedAnswerIndex = null,
            correctAnswers = 0,
            wrongAnswers = 0,
            earnedCoins = 0,
            roundId = System.currentTimeMillis()
        )
    }

    fun selectAnswer(index: Int) {
        if (state.phase != QuizSoloPhase.QUESTION || state.selectedAnswerIndex != null) return
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        val correct = index == question.correctAnswerIndex
        state = state.copy(
            selectedAnswerIndex = index,
            correctAnswers = state.correctAnswers + if (correct) 1 else 0,
            wrongAnswers = state.wrongAnswers + if (correct) 0 else 1
        )
    }

    fun nextQuestion() {
        if (state.currentIndex >= state.questions.lastIndex) {
            finishQuiz()
        } else {
            state = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedAnswerIndex = null
            )
        }
    }

    fun finishQuiz() {
        val earned = QuizSoloRewardRules.calculate(state.correctAnswers, state.questions.size)
        val finished = state.copy(phase = QuizSoloPhase.FINISHED, earnedCoins = earned)
        state = finished
        saveQuizSoloRoundHistory(context, finished.toHistory(userId))
    }

    suspend fun restartQuiz() {
        startQuiz(state.selectedSourceId)
    }

    private fun QuizSoloUiState.toHistory(userId: Long) = QuizSoloRoundHistory(
        id = roundId,
        userId = userId,
        totalQuestions = questions.size,
        correctAnswers = correctAnswers,
        wrongAnswers = wrongAnswers,
        earnedCoins = earnedCoins,
        completedAt = LocalDateTime.now().toString()
    )

    companion object {
        const val ROUND_SIZE = 10
    }
}

@Composable
fun QuizSoloScreen(
    api: EducationalApi,
    userId: Long,
    onBack: () -> Unit,
    onCoinsBalanceUpdated: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = remember(api, userId) {
        QuizSoloViewModel(QuizQuestionRepository(api), userId, context)
    }
    val state = viewModel.state
    var rewardSyncedRoundId by remember { mutableStateOf<Long?>(null) }
    var rewardMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        viewModel.startQuiz()
    }

    LaunchedEffect(state.phase, state.roundId, state.earnedCoins) {
        if (state.phase == QuizSoloPhase.FINISHED && rewardSyncedRoundId != state.roundId && state.earnedCoins > 0) {
            rewardSyncedRoundId = state.roundId
            runCatching {
                api.rewardCoins(
                    userId = userId,
                    amount = state.earnedCoins,
                    reason = "Quiz Solo: ${state.correctAnswers}/${state.questions.size} acertos"
                )
            }.onSuccess { newBalance ->
                onCoinsBalanceUpdated(newBalance)
                rewardMessage = "Pontos adicionados ao seu saldo."
            }.onFailure {
                rewardMessage = "Pontos calculados. Sincronize quando estiver online."
            }
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (state.phase) {
            QuizSoloPhase.LOADING -> QuizSoloLoading()
            QuizSoloPhase.EMPTY -> QuizSoloEmpty(onRetry = { scope.launch { viewModel.restartQuiz() } })
            QuizSoloPhase.ERROR -> QuizSoloError(
                message = state.errorMessage ?: "Não foi possível carregar o quiz. Tente novamente.",
                onRetry = { scope.launch { viewModel.restartQuiz() } }
            )
            QuizSoloPhase.READY -> QuizSoloStart(
                state = state,
                onSelectSource = { sourceId -> scope.launch { viewModel.chooseSource(sourceId) } },
                onStart = { viewModel.beginRound() }
            )
            QuizSoloPhase.QUESTION -> QuizSoloQuestion(
                state = state,
                onSelect = { viewModel.selectAnswer(it) },
                onNext = { viewModel.nextQuestion() }
            )
            QuizSoloPhase.FINISHED -> QuizSoloResult(
                state = state,
                rewardMessage = rewardMessage,
                onRestart = {
                    rewardMessage = null
                    scope.launch { viewModel.restartQuiz() }
                },
                onBack = onBack
            )
        }

        BottomBackButton(onBack = onBack)
    }
}

@Composable
private fun QuizSoloLoading() {
    Text("Quiz Solo", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
    Text("Carregando perguntas do banco...", color = LearnerMutedText)
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)),
        color = LearnerPrimary,
        trackColor = LearnerPrimaryTint
    )
}

@Composable
private fun QuizSoloEmpty(onRetry: () -> Unit) {
    Text("Quiz Solo", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
    EmptyStateCard(
        title = "Nenhuma pergunta salva ainda.",
        subtitle = "Gere perguntas, salve no Banco de Perguntas e depois volte aqui para jogar."
    )
    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Tentar novamente") }
}

@Composable
private fun QuizSoloError(message: String, onRetry: () -> Unit) {
    Text("Quiz Solo", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
    ErrorStateCard(error = message.toFriendlyError(), onRetry = onRetry)
}

@Composable
private fun QuizSoloStart(
    state: QuizSoloUiState,
    onSelectSource: (Long?) -> Unit,
    onStart: () -> Unit
) {
    val estimatedReward = QuizSoloRewardRules.calculate(state.questions.size, state.questions.size)
    Text("Quiz Solo", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
    Text("Escolha uma lista do Banco de Perguntas para montar a rodada.", color = LearnerMutedText)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Fonte das perguntas", fontWeight = FontWeight.Black, fontSize = 18.sp)
            state.sources.forEach { source ->
                QuizSourceCard(
                    source = source,
                    selected = source.id == state.selectedSourceId,
                    onClick = { onSelectSource(source.id) }
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(58.dp).background(LearnerPrimaryTint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QS", color = LearnerPrimary, fontWeight = FontWeight.Black)
                }
                Column {
                    Text(state.selectedSourceName, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("${state.questions.size} perguntas nesta rodada", color = LearnerMutedText)
                }
            }
            if (state.questions.isEmpty()) {
                Text("Essa lista ainda não tem perguntas. Adicione perguntas no Banco ou escolha outra lista.", color = LearnerCoral, fontWeight = FontWeight.Bold)
            } else {
                Text("Recompensa máxima: $estimatedReward pontos", color = LearnerTeal, fontWeight = FontWeight.Black)
                Text("A rodada usa perguntas salvas no Banco de Perguntas.", color = LearnerMutedText, fontSize = 12.sp)
            }
            Button(onClick = onStart, enabled = state.questions.isNotEmpty(), modifier = Modifier.fillMaxWidth()) { Text("Começar") }
        }
    }
}

@Composable
private fun QuizSourceCard(source: QuizQuestionSource, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) LearnerPrimary else LearnerPrimaryTint
    val titleColor = if (selected) Color.White else Color(0xFF111827)
    val subtitleColor = if (selected) Color.White.copy(alpha = .82f) else LearnerMutedText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(source.name, color = titleColor, fontWeight = FontWeight.Black)
            Text("${source.questionCount} perguntas", color = subtitleColor, fontSize = 12.sp)
        }
        Text(if (selected) "Selecionada" else "Usar", color = titleColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun QuizSoloQuestion(
    state: QuizSoloUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit
) {
    val question = state.questions[state.currentIndex]
    val selected = state.selectedAnswerIndex
    val answered = selected != null
    val progress = (state.currentIndex + if (answered) 1 else 0).toFloat() / state.questions.size.toFloat()

    Text("Quiz Solo", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
    Text(state.selectedSourceName, color = LearnerMutedText, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        QuizSoloMetric("${state.currentIndex + 1}/${state.questions.size}", "pergunta", Modifier.weight(1f))
        QuizSoloMetric("${state.correctAnswers}", "acertos", Modifier.weight(1f))
        QuizSoloMetric("${QuizSoloRewardRules.calculate(state.correctAnswers, state.questions.size)}", "pontos", Modifier.weight(1f))
    }
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(99.dp)),
        color = LearnerPrimary,
        trackColor = LearnerPrimaryTint
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(question.questionText, fontWeight = FontWeight.Black, fontSize = 20.sp)
            question.options.forEachIndexed { index, option ->
                val isCorrect = index == question.correctAnswerIndex
                val isSelected = selected == index
                val background = when {
                    answered && isCorrect -> LearnerTeal.copy(alpha = .18f)
                    answered && isSelected && !isCorrect -> LearnerCoral.copy(alpha = .18f)
                    else -> LearnerPrimaryTint
                }
                val borderText = when {
                    answered && isCorrect -> LearnerTeal
                    answered && isSelected && !isCorrect -> LearnerCoral
                    else -> LearnerPrimary
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(background)
                        .clickable(enabled = !answered) { onSelect(index) }
                        .padding(14.dp)
                ) {
                    Text(
                        text = "${'A' + index}. $option",
                        color = borderText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (answered) {
                val won = selected == question.correctAnswerIndex
                Text(
                    text = if (won) "Você acertou! +${QuizSoloRewardRules.COINS_PER_CORRECT} pontos" else "Quase. Resposta correta: ${question.options[question.correctAnswerIndex]}",
                    color = if (won) LearnerTeal else LearnerCoral,
                    fontWeight = FontWeight.Black
                )
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.currentIndex == state.questions.lastIndex) "Finalizar" else "Próxima")
                }
            }
        }
    }
}

@Composable
private fun QuizSoloResult(
    state: QuizSoloUiState,
    rewardMessage: String?,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    val total = state.questions.size
    val accuracy = if (total == 0) 0 else ((state.correctAnswers.toFloat() / total.toFloat()) * 100).roundToInt()
    Text("Quiz concluído", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(92.dp).background(LearnerPrimaryTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$accuracy%", color = LearnerPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
            }
            Text("Você acertou ${state.correctAnswers} de $total perguntas.", fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("Erros: ${state.wrongAnswers}", color = LearnerMutedText)
            Text("Pontos ganhos: ${state.earnedCoins}", color = LearnerTeal, fontWeight = FontWeight.Black, fontSize = 20.sp)
            rewardMessage?.let { Text(it, color = LearnerMutedText, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
            Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Jogar novamente") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Voltar para Jogos") }
        }
    }
}

@Composable
private fun QuizSoloMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = LearnerPrimary)
            Text(label, color = LearnerMutedText, fontSize = 12.sp)
        }
    }
}

private fun saveQuizSoloRoundHistory(context: Context, history: QuizSoloRoundHistory) {
    val prefs = context.getSharedPreferences("quiz_solo_history", Context.MODE_PRIVATE)
    val raw = prefs.getString("rounds_${history.userId}", "[]") ?: "[]"
    val array = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
    array.put(history.toJson())
    prefs.edit().putString("rounds_${history.userId}", array.toString()).apply()
}

private fun QuizSoloRoundHistory.toJson() = JSONObject()
    .put("id", id)
    .put("userId", userId)
    .put("totalQuestions", totalQuestions)
    .put("correctAnswers", correctAnswers)
    .put("wrongAnswers", wrongAnswers)
    .put("earnedCoins", earnedCoins)
    .put("completedAt", completedAt)
