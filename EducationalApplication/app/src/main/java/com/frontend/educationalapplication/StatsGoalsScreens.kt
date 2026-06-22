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
import androidx.compose.ui.graphics.Path
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
fun GoalsMissionsScreen(
    api: EducationalApi,
    goals: List<GoalResponse>,
    missions: List<MissionResponse>,
    onClaimed: (Int) -> Unit
) {
    GoalsScreen(goals)
    MissionsScreen(api, missions, onClaimed)
}


@Composable
fun StatsScreen(progress: ProgressResponse?) {
    // Progress contains already-aggregated totals from the backend.
    SectionCard("Progresso") {
        if (progress == null) {
            Text("Ainda não há dados")
            return@SectionCard
        }
        Text("${progress.totalMinutes} minutos estudados nesta semana", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Por matéria")
        progress.bySubject.forEach { item ->
            BarLine(label = item.subject, value = item.minutes, max = progress.totalMinutes.coerceAtLeast(1))
        }
        Spacer(Modifier.height(8.dp))
        Text("Por dia")
        progress.byDay.forEach { item ->
            BarLine(label = item.date.takeLast(5), value = item.minutes, max = progress.totalMinutes.coerceAtLeast(1))
        }
    }
}

@Composable
fun GoalsScreen(goals: List<GoalResponse>) {
    // Goals are read-only in the prototype frontend for now.
    SectionCard("Metas") {
        goals.forEach { goal ->
            ListLine(
                title = goal.title,
                detail = "${goal.currentMinutes}/${goal.targetMinutes} min - D-${goal.daysUntilDueDate}"
            )
            LinearProgressIndicator(
                progress = { (goal.progressPercent / 100.0).toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun MissionsScreen(
    api: EducationalApi,
    missions: List<MissionResponse>,
    onClaimed: (Int) -> Unit
) {
    // Missions can be claimed when the backend says they are completed.
    SectionCard("Missões") {
        val scope = rememberCoroutineScope()
        missions.forEach { mission ->
            ListLine(
                title = mission.title,
                detail = "${mission.progressMinutes}/${mission.targetMinutes} min - ${mission.rewardCoins} moedas"
            )
            Button(
                enabled = mission.completed && !mission.claimed,
                onClick = {
                    scope.apiCall(
                        action = { api.claimMission(mission.id) },
                        onSuccess = { onClaimed(it.rewardCoins) },
                        onError = {}
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (mission.claimed) "Resgatada" else "Resgatar")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProgressDashboard(progress: ProgressResponse?, goals: List<GoalResponse>) {
    val totalMinutes = progress?.totalMinutes ?: 0
    val activeDays = progress?.byDay?.count { it.minutes > 0 } ?: 0
    val subjects = progress?.bySubject?.count { it.minutes > 0 } ?: 0
    val completedGoals = goals.count { it.completed }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(
            title = "Estatisticas",
            subtitle = "Veja como o seu estudo evolui ao longo da semana"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatisticMetricCard(
                value = "$activeDays",
                label = "Dias ativos",
                accent = LearnerCoral,
                modifier = Modifier.weight(1f)
            )
            StatisticMetricCard(
                value = "$totalMinutes min",
                label = "Tempo estudado",
                accent = LearnerPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatisticMetricCard(
                value = "$subjects",
                label = "Materias estudadas",
                accent = LearnerTeal,
                modifier = Modifier.weight(1f)
            )
            StatisticMetricCard(
                value = "$completedGoals",
                label = "Metas concluidas",
                accent = LearnerYellow,
                modifier = Modifier.weight(1f)
            )
        }

        val hasStudyData = progress?.byDay?.any { it.minutes > 0 } == true
        if (hasStudyData) {
            WeeklyStudyChart(progress!!.byDay.map { it.date.takeLast(5) to it.minutes })
            ProgressBreakdownCard(
                title = "Tempo por materia",
                items = progress.bySubject.map { it.subject to it.minutes },
                max = totalMinutes.coerceAtLeast(1),
                accent = LearnerPrimary
            )
        } else {
            WeeklyStudyChart(
                days = listOf("S" to 0, "T" to 0, "Q" to 0, "Q" to 0, "S" to 0, "S" to 0, "D" to 135),
                isPreview = true
            )
        }
    }
}

@Composable
fun StatisticMetricCard(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WeeklyStudyChart(days: List<Pair<String, Int>>, isPreview: Boolean = false) {
    val maxMinutes = (days.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (isPreview) "Exemplo de grafico semanal" else "Estudo nesta semana",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(LearnerPrimary))
                    Text(if (isPreview) "Como seus dados aparecem" else "Seu estudo", color = LearnerPrimary, fontWeight = FontWeight.Bold)
                }
                Text("${days.maxOfOrNull { it.second } ?: 0} min", color = LearnerPrimary, fontWeight = FontWeight.Bold)
            }
            if (isPreview) {
                Text(
                    "A amostra e substituida assim que voce registrar uma sessao.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.height(170.dp).width(32.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(maxMinutes, maxMinutes * 2 / 3, maxMinutes / 3, 0).forEach { value ->
                        Text("$value", style = MaterialTheme.typography.labelMedium, color = LearnerMutedText)
                    }
                }
                Canvas(modifier = Modifier.weight(1f).height(170.dp)) {
                    val chartTop = size.height * 0.02f
                    val chartBottom = size.height * 0.98f
                    repeat(4) { index ->
                        val y = chartTop + ((chartBottom - chartTop) * index / 3f)
                        drawLine(
                            color = LearnerPrimaryTint,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 2f
                        )
                    }
                    val path = Path()
                    days.forEachIndexed { index, (_, minutes) ->
                        val x = if (days.size == 1) size.width / 2f else size.width * index / (days.size - 1).toFloat()
                        val y = chartBottom - ((minutes.toFloat() / maxMinutes) * (chartBottom - chartTop))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = LearnerPrimary, style = Stroke(width = 4f))
                    days.forEachIndexed { index, (_, minutes) ->
                        val x = if (days.size == 1) size.width / 2f else size.width * index / (days.size - 1).toFloat()
                        val y = chartBottom - ((minutes.toFloat() / maxMinutes) * (chartBottom - chartTop))
                        drawCircle(color = LearnerPrimary, radius = 7f, center = Offset(x, y))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(start = 32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { (label, _) ->
                    Text(label, style = MaterialTheme.typography.labelMedium, color = LearnerMutedText)
                }
            }
        }
    }
}

@Composable
fun ProgressDashboardHeader(goals: List<GoalResponse>) {
    val primaryGoal = goals.firstOrNull()
    val goalProgress = ((primaryGoal?.progressPercent ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Progresso",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = primaryGoal?.let {
                    "${it.currentMinutes}/${it.targetMinutes} min na meta atual"
                } ?: "Defina uma meta para acompanhar sua evolucao",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (primaryGoal != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { goalProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = LearnerTeal,
                    trackColor = LearnerTeal.copy(alpha = 0.15f)
                )
            }
        }
        Spacer(Modifier.width(18.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(LearnerYellow.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${(goalProgress * 100).toInt()}%",
                color = LearnerYellow,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GoalsMissionsDashboard(
    api: EducationalApi,
    goals: List<GoalResponse>,
    missions: List<MissionResponse>,
    onClaimed: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(
            title = "Metas e missoes",
            subtitle = "Transforme seus estudos em pequenos objetivos"
        )

        if (goals.isEmpty() && missions.isEmpty()) {
            EmptyStateCard(
                title = "Nada por aqui ainda",
                subtitle = "Suas metas e missoes aparecerao conforme voce avancar nos estudos."
            )
            return@Column
        }

        goals.forEachIndexed { index, goal ->
            DashboardAccentCard(
                title = goal.title,
                detail = "${goal.currentMinutes}/${goal.targetMinutes} min - faltam ${goal.daysUntilDueDate} dias",
                accent = if (index % 2 == 0) Color(0xFF6A63FF) else Color(0xFF4674F5),
                progress = (goal.progressPercent / 100.0).toFloat(),
                emphasized = true
            )
        }

        missions.forEachIndexed { index, mission ->
            MissionDashboardCard(
                mission = mission,
                accent = if (index % 2 == 0) Color(0xFFE88CB9) else Color(0xFF7A6BEA),
                onClaim = {
                    scope.apiCall(
                        action = { api.claimMission(mission.id) },
                        onSuccess = { onClaimed(it.rewardCoins) },
                        onError = {}
                    )
                }
            )
        }
    }
}

@Composable
fun DashboardAccentCard(
    title: String,
    detail: String,
    accent: Color,
    progress: Float? = null,
    emphasized: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (emphasized) accent else accent.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                title,
                color = if (emphasized) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = if (emphasized) CircularDisplayFamily else null
            )
            Text(
                detail,
                color = if (emphasized) Color.White.copy(alpha = 0.94f) else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasized) FontWeight.Medium else FontWeight.Normal,
                fontFamily = if (emphasized) CircularDisplayFamily else null
            )
            progress?.let {
                LinearProgressIndicator(
                    progress = { it.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(99.dp)),
                    color = if (emphasized) Color.White else accent,
                    trackColor = if (emphasized) Color.White.copy(alpha = 0.30f) else accent.copy(alpha = 0.18f)
                )
            }
        }
    }
}

@Composable
fun ProgressBreakdownCard(
    title: String,
    items: List<Pair<String, Int>>,
    max: Int,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
            if (items.isEmpty()) {
                Text("Sem dados no periodo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                items.forEach { (label, value) ->
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    LinearProgressIndicator(
                        progress = { (value.toFloat() / max).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(99.dp)),
                        color = accent,
                        trackColor = accent.copy(alpha = 0.15f)
                    )
                    Text("$value min", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun MissionDashboardCard(mission: MissionResponse, accent: Color, onClaim: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(mission.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, fontFamily = CircularDisplayFamily)
            Text(
                "${mission.progressMinutes}/${mission.targetMinutes} min - ${mission.rewardCoins} moedas",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontFamily = CircularDisplayFamily
            )
            Button(
                enabled = mission.completed && !mission.claimed,
                onClick = onClaim,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = accent,
                    disabledContainerColor = Color.White.copy(alpha = 0.40f),
                    disabledContentColor = accent.copy(alpha = 0.65f)
                )
            ) {
                Text(if (mission.claimed) "Resgatada" else "Resgatar")
            }
        }
    }
}

@Composable
fun ReviewsScreen(
    api: EducationalApi,
    reviews: List<ReviewReminderResponse>,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pending = reviews.count { !it.completed }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(
            title = "Revisões",
            subtitle = "$pending lembretes pendentes"
        )

        SummaryCard(
            title = "$pending",
            subtitle = if (pending == 1) "revisão para fazer" else "revisões para fazer",
            accentColor = LearnerTeal
        )

        if (reviews.isEmpty()) {
            EmptyStateCard(
                title = "Sem revisões por enquanto",
                subtitle = "Salve uma sessão de estudo para criar lembretes automaticamente."
            )
            return@Column
        }

        reviews.forEach { review ->
            ReviewReminderCard(
                review = review,
                onComplete = {
                    scope.apiCall(
                        action = { api.completeReview(review.id) },
                        onSuccess = { onDone() },
                        onError = {}
                    )
                }
            )
        }
    }
}


