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
import androidx.compose.ui.platform.LocalContext
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
import com.frontend.educationalapplication.network.RankingResponse
import com.frontend.educationalapplication.network.RankingPlayerResponse
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
            Text("Ainda nao ha dados")
            return@SectionCard
        }
        Text("${progress.totalMinutes} minutos estudados nesta semana", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Por materia")
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
    SectionCard("Missoes") {
        val scope = rememberCoroutineScope()
        missions.forEach { mission ->
            ListLine(
                title = mission.title,
                detail = "${mission.progressMinutes}/${mission.targetMinutes} min - ${mission.rewardCoins} pontos"
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
fun ProgressDashboard(
    progress: ProgressResponse?,
    goals: List<GoalResponse>,
    onOpenRanking: () -> Unit
) {
    val totalMinutes = progress?.totalMinutes ?: 0
    val activeDays = progress?.byDay?.count { it.minutes > 0 } ?: 0
    val subjects = progress?.bySubject?.count { it.minutes > 0 } ?: 0
    val completedGoals = goals.count { it.completed }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        RankingMenuCard(onOpenRanking = onOpenRanking)
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
private fun RankingMenuCard(onOpenRanking: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenRanking),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF292059)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            RobotAvatar(seed = 0, avatarSize = 54.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Ranking da comunidade", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Compare seu ritmo de estudos com outros alunos", color = Color.White.copy(alpha = .72f), fontSize = 13.sp)
            }
            Text(">", color = LearnerYellow, fontSize = 32.sp)
        }
    }
}

@Composable
fun RankingDashboard(api: EducationalApi, userId: Long, onBack: () -> Unit) {
    var rankingPeriod by remember { mutableStateOf(RankingPeriod.Week) }
    var ranking by remember { mutableStateOf<RankingResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadRanking() {
        if (userId <= 0 || BuildConfig.DEMO_MODE) {
            ranking = demoRanking(
                period = if (rankingPeriod == RankingPeriod.Week) "WEEK" else "ALL_TIME",
                currentUserId = userId
            )
            loading = false
            error = null
            return
        }
        loading = true
        error = null
        scope.apiCall(
            action = {
                if (rankingPeriod == RankingPeriod.Week) {
                    api.rankingWeekly(userId)
                } else {
                    api.rankingAllTime(userId)
                }
            },
            onSuccess = {
                ranking = it
                loading = false
            },
            onError = {
                error = it
                loading = false
            }
        )
    }

    LaunchedEffect(rankingPeriod, userId) {
        loadRanking()
    }

    val players = ranking?.players.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Ranking", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(if (rankingPeriod == RankingPeriod.Week) "Tempo estudado nesta semana" else "Tempo acumulado desde o inicio", color = LearnerMutedText)
        RankingPeriodToggle(selected = rankingPeriod, onSelected = { rankingPeriod = it })
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)),
                color = LearnerPrimary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
        error?.let {
            ErrorStateCard(error = it.toFriendlyError(), onRetry = { loadRanking() })
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2B5)),
            shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RankingHeaderBadge()
                Column {
                    Text(if (rankingPeriod == RankingPeriod.Week) "Desafio semanal" else "Ranking historico", fontWeight = FontWeight.Black)
                    Text("Dados de demonstracao - serao substituidos pelos dados reais.", color = LearnerMutedText, fontSize = 13.sp)
                }
            }
        }
        if (!loading && error == null && players.isEmpty()) {
            EmptyStateCard(
                title = "Ranking vazio por enquanto",
                subtitle = "Quando alguem registrar sessoes de estudo, o ranking aparece aqui."
            )
        }
        players.forEach { player -> RankingRow(player = player) }
        BottomBackButton(onBack = onBack)
    }
}

private enum class RankingPeriod { Week, AllTime }

@Composable
private fun RankingPeriodToggle(selected: RankingPeriod, onSelected: (RankingPeriod) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFEDEBFA), RoundedCornerShape(14.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RankingPeriod.values().forEach { period ->
            val isSelected = selected == period
            Text(
                text = if (period == RankingPeriod.Week) "Semana" else "Todo o tempo",
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (isSelected) LearnerPrimary else Color.Transparent).clickable { onSelected(period) }.padding(vertical = 10.dp),
                color = if (isSelected) Color.White else LearnerMutedText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RankingHeaderBadge() {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(LearnerYellow),
        contentAlignment = Alignment.Center
    ) {
        Text("TOP", color = Color(0xFF5C4300), fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
private fun RankingPositionBadge(position: Int) {
    val badgeColor = when (position) {
        1 -> Color(0xFFFFD54F)
        2 -> Color(0xFFC9CED6)
        3 -> Color(0xFFD89A5B)
        else -> Color(0xFFEDEBFA)
    }
    val textColor = if (position <= 3) Color(0xFF2E2A1F) else LearnerMutedText
    Box(
        modifier = Modifier
            .width(34.dp)
            .height(34.dp)
            .clip(CircleShape)
            .background(badgeColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (position <= 3) "$position" else "#$position",
            color = textColor,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            fontSize = if (position <= 3) 15.sp else 13.sp
        )
    }
}

@Composable
private fun RankingRow(player: RankingPlayerResponse) {
    val isCurrentUser = player.currentUser
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isCurrentUser) LearnerPrimaryTint else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RankingPositionBadge(position = player.position)
            RobotAvatar(seed = (player.userId % 5).toInt(), avatarSize = 42.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(player.name, fontWeight = FontWeight.Black)
                Text(if (isCurrentUser) "Seu ritmo atual" else "Estudando esta semana", color = LearnerMutedText, fontSize = 12.sp)
            }
            Text("${player.minutes} min", color = LearnerPrimary, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RobotAvatar(seed: Int, avatarSize: androidx.compose.ui.unit.Dp) {
    val colors = listOf(Color(0xFF635BFF), Color(0xFFE85D9C), Color(0xFF1FA9B5), Color(0xFFFF9F1C), Color(0xFF61B85C))
    val body = colors[seed % colors.size]
    Canvas(modifier = Modifier.size(avatarSize)) {
        val unit = minOf(size.width, size.height)
        drawCircle(body, radius = unit * .48f, center = Offset(unit * .5f, unit * .5f))
        drawRoundRect(Color(0xFF20213A), Offset(unit * .18f, unit * .27f), Size(unit * .64f, unit * .48f), CornerRadius(unit * .14f, unit * .14f))
        drawCircle(Color(0xFFE9FFFF), radius = unit * .09f, center = Offset(unit * .38f, unit * .5f))
        drawCircle(Color(0xFFE9FFFF), radius = unit * .09f, center = Offset(unit * .62f, unit * .5f))
        drawCircle(body, radius = unit * .035f, center = Offset(unit * .38f, unit * .5f))
        drawCircle(body, radius = unit * .035f, center = Offset(unit * .62f, unit * .5f))
        drawLine(Color(0xFF20213A), Offset(unit * .5f, unit * .27f), Offset(unit * .5f, unit * .12f), strokeWidth = unit * .06f)
        drawCircle(LearnerYellow, radius = unit * .08f, center = Offset(unit * .5f, unit * .1f))
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
                    "A amostra e substitu?da assim que voc? registrar uma sessão.",
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
    userId: Long,
    sessions: List<StudySessionResponse>,
    goals: List<GoalResponse>,
    missions: List<MissionResponse>,
    onClaimed: (Int) -> Unit
) {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }
    val missionState = remember(userId, sessions, refreshKey) {
        buildMissionGamificationState(context, userId, sessions)
    }
    var showCustomGoalForm by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(
            title = "Metas e Missões",
            subtitle = "Ganhe pontos com sessões reais de estudo"
        )

        MissionPointsSummaryCard(points = missionState.points)

        SectionCard("Missões do app") {
            Text("Só contam sessões salvas com 10 minutos ou mais.", color = LearnerMutedText, fontSize = 12.sp)
            missionState.missions.forEachIndexed { index, mission ->
                MissionRuleCard(
                    mission = mission,
                    accent = if (index % 2 == 0) Color(0xFF6A63FF) else Color(0xFFE88CB9)
                )
            }
        }

        SectionCard("Metas personalizadas") {
            Text("Metas personalizadas servem só para acompanhamento. Elas não dão pontos.", color = LearnerMutedText, fontSize = 12.sp)
            if (showCustomGoalForm) {
                CustomGoalForm(
                    onCancel = { showCustomGoalForm = false },
                    onSave = { title, description, period, target ->
                        createCustomMissionGoal(context, userId, title, description, period, target)
                        showCustomGoalForm = false
                        refreshKey += 1
                    }
                )
            } else {
                Button(onClick = { showCustomGoalForm = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Criar meta personalizada")
                }
            }
            if (missionState.customGoals.isEmpty()) {
                EmptyStateCard("Nenhuma meta personalizada", "Crie uma meta leve para acompanhar um objetivo próprio.")
            } else {
                missionState.customGoals.forEach { goal -> CustomGoalCard(goal) }
            }
        }
    }
}

@Composable
private fun MissionPointsSummaryCard(points: UserPointsUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF292059))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Pontuação", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MissionSummaryMetric("${points.totalPoints}", "total", Modifier.weight(1f))
                MissionSummaryMetric("${points.pointsToday}", "hoje", Modifier.weight(1f))
                MissionSummaryMetric("${points.currentStudyStreak}", "dias", Modifier.weight(1f))
            }
            Text(
                "Pontos oficiais: ${points.officialPoints} pts • Metas personalizadas não pontuam • Semanas seguidas: ${points.weeklyStreak}",
                color = Color.White.copy(alpha = .75f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MissionSummaryMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = .12f))
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text(label, color = Color.White.copy(alpha = .72f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun MissionRuleCard(mission: AppMissionUi, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = .12f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(mission.name, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text(mission.description, color = LearnerMutedText, fontSize = 12.sp)
                }
                Text("+${mission.rewardPoints} pts", color = accent, fontWeight = FontWeight.Black)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LinearProgressIndicator(
                    progress = { mission.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(99.dp)),
                    color = accent,
                    trackColor = accent.copy(alpha = .18f)
                )
                Text(mission.progressText, color = LearnerMutedText, fontSize = 12.sp)
            }
            Text(mission.status, color = if (mission.status == "Resgatada") LearnerTeal else accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CustomGoalCard(goal: CustomGoalUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column {
                Text(goal.title, fontWeight = FontWeight.Black)
                if (goal.description.isNotBlank()) Text(goal.description, color = LearnerMutedText, fontSize = 12.sp)
            }
            LinearProgressIndicator(
                progress = { goal.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)),
                color = LearnerTeal,
                trackColor = LearnerTeal.copy(alpha = .15f)
            )
            Text("${goal.progressText} • ${goal.period} • ${if (goal.completed) "Concluída" else "Em andamento"}", color = LearnerMutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CustomGoalForm(
    onCancel: () -> Unit,
    onSave: (String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("WEEK") }
    var target by remember { mutableStateOf("3") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nome da meta") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição curta") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("DAY" to "Hoje", "WEEK" to "Semana", "MONTH" to "Mês").forEach { (value, label) ->
                MissionPeriodChip(label, period == value, Modifier.weight(1f)) { period = value }
            }
        }
        OutlinedTextField(value = target, onValueChange = { target = it.filter(Char::isDigit).take(2) }, label = { Text("Sessões para concluir") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                enabled = title.trim().isNotBlank(),
                onClick = { onSave(title, description, period, target.toIntOrNull() ?: 1) },
                modifier = Modifier.weight(1f)
            ) { Text("Salvar") }
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancelar") }
        }
    }
}

@Composable
private fun MissionPeriodChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                color = if (emphasized) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
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
    val progress = if (mission.targetMinutes > 0) {
        mission.progressMinutes.toFloat() / mission.targetMinutes.toFloat()
    } else {
        0f
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(mission.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, fontFamily = CircularDisplayFamily)
            Text(
                "${mission.progressMinutes}/${mission.targetMinutes} min - ${mission.rewardCoins} pontos",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontFamily = CircularDisplayFamily
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(99.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.30f)
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
                Text(if (mission.claimed) "Pontos resgatados" else "Resgatar pontos")
            }
        }
    }
}

private fun goalRewardPoints(goal: GoalResponse): Int =
    (goal.targetMinutes / 5).coerceAtLeast(10)

@Composable
fun ReviewsScreen(
    api: EducationalApi,
    userId: Long,
    reviews: List<ReviewReminderResponse>,
    onDone: () -> Unit,
    onStartStudy: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val pending = reviews.count { !it.completed }
    var showCreateForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)) }
    var formMessage by remember { mutableStateOf<String?>(null) }

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

        Button(
            onClick = {
                showCreateForm = !showCreateForm
                formMessage = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (showCreateForm) "Fechar formulario" else "+ Criar revisão")
        }

        if (showCreateForm) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Nova revisão", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            formMessage = null
                        },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {
                            subject = it
                            formMessage = null
                        },
                        label = { Text("Matéria") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {
                            dueDate = it.take(10)
                            formMessage = null
                        },
                        label = { Text("Data da revisão (AAAA-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    formMessage?.let { Text(it, color = LearnerCoral, fontWeight = FontWeight.Bold) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (title.isBlank() || subject.isBlank() || dueDate.length != 10) {
                                    formMessage = "Preencha título, matéria e data no formato AAAA-MM-DD."
                                } else {
                                    scope.apiCall(
                                        action = { api.createReview(userId, title.trim(), subject.trim(), dueDate.trim()) },
                                        onSuccess = {
                                            title = ""
                                            subject = ""
                                            showCreateForm = false
                                            onDone()
                                        },
                                        onError = { formMessage = it }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar")
                        }
                        TextButton(
                            onClick = {
                                showCreateForm = false
                                formMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }

        if (reviews.isEmpty()) {
            EmptyStateCard(
                title = "Sem revisões por enquanto",
                subtitle = "Salve uma sessão de estudo ou crie uma revisão manualmente."
            )
            return@Column
        }

        reviews.forEach { review ->
            ReviewReminderCard(
                review = review,
                onStartReview = {
                    savePomodoroPrefill(
                        context,
                        PomodoroPrefill(
                            subject = review.subject,
                            topic = review.topic.orEmpty(),
                            studyType = "Revisão",
                            taskTitle = review.taskTitle ?: review.title
                        )
                    )
                    onStartStudy()
                },
                onComplete = {
                    scope.apiCall(
                        action = { api.completeReview(review.id) },
                        onSuccess = { onDone() },
                        onError = {}
                    )
                },
                onDelete = {
                    scope.apiCall(
                        action = { api.deleteReview(review.id) },
                        onSuccess = { onDone() },
                        onError = {}
                    )
                }
            )
        }
    }
}


