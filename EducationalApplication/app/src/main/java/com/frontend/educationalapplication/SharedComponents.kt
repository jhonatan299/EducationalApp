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
import androidx.compose.ui.graphics.drawscope.rotate
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
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SummaryCard(title: String, subtitle: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.88f, size.height * 0.18f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 0.08f, size.height * 0.95f)
                )
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.86f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(LearnerPrimaryTint),
                contentAlignment = Alignment.Center
            ) {
                NavGlyph(index = 2, color = LearnerPrimary)
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BottomBackButton(
    onBack: () -> Unit,
    label: String = "Voltar",
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onBack,
        modifier = modifier
    ) {
        Text("‹ $label", color = LearnerPrimary, fontWeight = FontWeight.Bold)
    }
}

enum class FriendlyErrorCause {
    NoInternet,
    Server,
    NotFound,
    Unauthorized,
    Unknown
}

data class FriendlyError(
    val cause: FriendlyErrorCause,
    val title: String,
    val description: String,
    val actionLabel: String = "Tentar novamente"
)

fun Throwable.toFriendlyError(): FriendlyError {
    val raw = listOfNotNull(message, cause?.message, javaClass.simpleName).joinToString(" ").lowercase()
    return when {
        this is UnknownHostException ||
            this is SocketTimeoutException ||
            "unable to resolve host" in raw ||
            "failed to connect" in raw ||
            "timeout" in raw ||
            "network is unreachable" in raw -> FriendlyError(
                cause = FriendlyErrorCause.NoInternet,
                title = "Sem internet",
                description = "Parece que o app nao conseguiu conectar. Verifique sua internet e tente novamente."
            )

        "404" in raw || "not found" in raw -> FriendlyError(
            cause = FriendlyErrorCause.NotFound,
            title = "Nao encontramos isso",
            description = "Esse conteudo pode ter sido removido, movido ou ainda nao estar disponivel."
        )

        "401" in raw || "403" in raw || "unauthorized" in raw || "forbidden" in raw -> FriendlyError(
            cause = FriendlyErrorCause.Unauthorized,
            title = "Sessao expirada",
            description = "Sua sessão pode ter expirado. Entre novamente para continuar.",
            actionLabel = "Voltar ao login"
        )

        "500" in raw || "502" in raw || "503" in raw || "504" in raw ||
            "connection refused" in raw ||
            "server" in raw -> FriendlyError(
                cause = FriendlyErrorCause.Server,
                title = "Servidor indisponivel",
                description = "Nosso servidor nao respondeu como esperado. Tente de novo em alguns instantes."
            )

        else -> FriendlyError(
            cause = FriendlyErrorCause.Unknown,
            title = "Algo saiu do caminho",
            description = "Nao conseguimos concluir essa acao agora. Tente novamente daqui a pouco."
        )
    }
}

fun String.toFriendlyError(): FriendlyError =
    IllegalStateException(this).toFriendlyError()

@Composable
fun ErrorStateCard(
    error: FriendlyError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onGoHome: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ErrorIllustration(error.cause)
            Text(
                text = error.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = error.description,
                style = MaterialTheme.typography.bodyMedium,
                color = LearnerMutedText,
                textAlign = TextAlign.Center
            )
            ErrorCausePill(error.cause)
            onRetry?.let {
                Button(onClick = it, modifier = Modifier.fillMaxWidth()) {
                    Text(error.actionLabel)
                }
            }
            onGoHome?.let {
                TextButton(onClick = it) {
                    Text("Voltar para o inicio", color = LearnerPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ErrorCausePill(cause: FriendlyErrorCause) {
    val label = when (cause) {
        FriendlyErrorCause.NoInternet -> "Causa: conexao com a internet"
        FriendlyErrorCause.Server -> "Causa: servidor indisponivel"
        FriendlyErrorCause.NotFound -> "Causa: conteudo nao encontrado"
        FriendlyErrorCause.Unauthorized -> "Causa: sessão ou permissão"
        FriendlyErrorCause.Unknown -> "Causa: erro inesperado"
    }
    Box(
        modifier = Modifier
            .background(LearnerCoral.copy(alpha = .12f), RoundedCornerShape(99.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = LearnerCoral, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ErrorIllustration(cause: FriendlyErrorCause) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .background(LearnerPrimaryTint, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(86.dp)) {
            when (cause) {
                FriendlyErrorCause.NoInternet -> {
                    rotate(-12f) {
                        drawRoundRect(
                            color = LearnerPrimary,
                            topLeft = Offset(size.width * .16f, size.height * .25f),
                            size = Size(size.width * .68f, size.height * .5f),
                            cornerRadius = CornerRadius(14f, 14f)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = 210f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = Offset(size.width * .3f, size.height * .34f),
                            size = Size(size.width * .4f, size.height * .32f),
                            style = Stroke(width = 5f)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = 215f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(size.width * .38f, size.height * .44f),
                            size = Size(size.width * .24f, size.height * .18f),
                            style = Stroke(width = 5f)
                        )
                        drawCircle(Color.White, radius = 4.5f, center = Offset(size.width * .5f, size.height * .63f))
                    }
                }

                FriendlyErrorCause.NotFound -> {
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension * .31f,
                        center = Offset(size.width * .42f, size.height * .38f),
                        style = Stroke(width = 8f)
                    )
                    drawLine(
                        color = LearnerPrimary,
                        start = Offset(size.width * .62f, size.height * .62f),
                        end = Offset(size.width * .84f, size.height * .84f),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = LearnerCoral,
                        start = Offset(size.width * .33f, size.height * .38f),
                        end = Offset(size.width * .51f, size.height * .38f),
                        strokeWidth = 6f
                    )
                }

                FriendlyErrorCause.Server -> {
                    repeat(3) { index ->
                        val top = size.height * (.22f + index * .19f)
                        drawRoundRect(
                            color = LearnerPrimary,
                            topLeft = Offset(size.width * .2f, top),
                            size = Size(size.width * .6f, size.height * .13f),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        drawCircle(Color.White, radius = 4f, center = Offset(size.width * .3f, top + size.height * .065f))
                    }
                    drawLine(
                        color = LearnerCoral,
                        start = Offset(size.width * .28f, size.height * .78f),
                        end = Offset(size.width * .72f, size.height * .78f),
                        strokeWidth = 7f
                    )
                }

                FriendlyErrorCause.Unauthorized -> {
                    drawRoundRect(
                        color = LearnerPrimary,
                        topLeft = Offset(size.width * .24f, size.height * .42f),
                        size = Size(size.width * .52f, size.height * .38f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawArc(
                        color = LearnerPrimary,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(size.width * .3f, size.height * .16f),
                        size = Size(size.width * .4f, size.height * .46f),
                        style = Stroke(width = 8f)
                    )
                    drawCircle(Color.White, radius = 5f, center = Offset(size.width * .5f, size.height * .62f))
                }

                FriendlyErrorCause.Unknown -> {
                    drawCircle(LearnerPrimary, radius = size.minDimension * .32f, center = Offset(size.width * .5f, size.height * .48f))
                    drawLine(
                        color = Color.White,
                        start = Offset(size.width * .5f, size.height * .3f),
                        end = Offset(size.width * .5f, size.height * .52f),
                        strokeWidth = 7f
                    )
                    drawCircle(Color.White, radius = 4.5f, center = Offset(size.width * .5f, size.height * .66f))
                }
            }
        }
    }
}

@Composable
fun CalendarDayCard(day: CalendarDayResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LearnerPrimaryTint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.takeLast(2),
                    color = LearnerPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = day.date,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${day.totalMinutes} minutos estudados",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                day.subjects.forEach { subject ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = subject.subject,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${subject.minutes} min",
                            color = LearnerPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewReminderCard(
    review: ReviewReminderResponse,
    onStartReview: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(if (review.completed) LearnerPrimaryTint else LearnerTeal.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                    NavGlyph(index = 5, color = if (review.completed) LearnerMutedText else LearnerTeal)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(review.subject.ifBlank { "Sem matéria" }, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    review.topic?.let { Text("Tópico: $it", color = LearnerMutedText, style = MaterialTheme.typography.bodySmall) }
                    review.taskTitle?.let { Text("Tarefa: $it", color = LearnerMutedText, style = MaterialTheme.typography.bodySmall) }
                }
            }
            Text("Revisão para: ${review.dueDate} • ${review.reviewStatusLabel()}", color = LearnerPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            review.originalDifficulty?.let { Text("Dificuldade original: ${it.toReviewDifficultyLabel()}", color = LearnerMutedText, style = MaterialTheme.typography.bodySmall) }
            review.notes?.takeIf { it.isNotBlank() }?.let { Text("Anotação: $it", color = LearnerMutedText, style = MaterialTheme.typography.bodySmall) }
            Button(enabled = !review.completed, onClick = onStartReview, modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(14.dp))) { Text("Iniciar revisão") }
            Button(enabled = !review.completed, onClick = onComplete, modifier = Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(14.dp))) { Text(if (review.completed) "Revisada" else "Marcar como concluída") }
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Eliminar revisão", color = LearnerCoral, fontWeight = FontWeight.Bold) }
        }
    }
}

private fun ReviewReminderResponse.reviewStatusLabel(): String {
    if (completed) return "concluída"
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    return if (dueDate < today) "atrasada" else "pendente"
}

private fun String.toReviewDifficultyLabel(): String = when (uppercase(Locale.getDefault())) {
    "HARD" -> "Difícil"
    "EASY" -> "Fácil"
    else -> "Ok"
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    // Reusable card wrapper so all sections share the same layout style.
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
fun ListLine(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(detail, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun BarLine(label: String, value: Int, max: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, modifier = Modifier.weight(0.34f), style = MaterialTheme.typography.bodySmall)
        Box(
            modifier = Modifier
                .height(12.dp)
                .weight(0.66f)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value.toFloat() / max).coerceIn(0.02f, 1f))
                    .height(12.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
            )
        }
        Text("$value", style = MaterialTheme.typography.bodySmall)
    }
}

fun timerText(seconds: Int): String {
    val minutes = seconds / 60
    val rest = seconds % 60
    return "%02d:%02d".format(minutes, rest)
}

fun String.toBackendStudyType(): String =
    when (trim().uppercase()) {
        "TEORIA", "LEITURA", "RESUMO", "OUTRO" -> "THEORY"
        "PRATICA", "PR?TICA", "EXERCICIO", "EXERC?CIO", "EXERCICIOS", "EXERC?CIOS" -> "EXERCISES"
        "REVISAO", "REVIS?O" -> "REVIEW"
        "VIDEOAULA", "V?DEOAULA" -> "VIDEO_LESSON"
        "SIMULADO" -> "MOCK_TEST"
        "" -> "THEORY"
        else -> trim().uppercase()
    }


fun String?.toStudyTypeLabel(): String? =
    when (this) {
        "THEORY" -> "Teoria"
        "EXERCISES" -> "Exercícios"
        "REVIEW" -> "Revisão"
        "VIDEO_LESSON" -> "Videoaula"
        "MOCK_TEST" -> "Simulado"
        else -> this
    }

fun String.toSessionSourceLabel(): String =
    when (this) {
        "MANUAL" -> "Manual"
        "POMODORO" -> "Pomodoro"
        else -> this
    }

fun <T> CoroutineScope.apiCall(
    action: suspend () -> T,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    // Small helper so every button can call the backend with the same success/error pattern.
    launch {
        val result = runCatching { action() }
        result.onSuccess(onSuccess)
        result.exceptionOrNull()?.let { onError(it.toFriendlyError().description) }
        return@launch

        runCatching { action() }
            .onSuccess(onSuccess)
            .onFailure { onError(it.message ?: "Falha na requisição") }
    }
}


