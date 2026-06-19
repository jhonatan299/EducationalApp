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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
@Composable
fun HomeScreen(
    api: EducationalApi,
    user: AuthResponse,
    isGuest: Boolean,
    onUserUpdated: (AuthResponse) -> Unit
) {
    val tabs = listOf("Home", "Relatório", "Calendário", "Estatísticas", "Metas")
    var selectedTab by remember { mutableIntStateOf(0) }
    var homeRoute by remember { mutableStateOf(HomeRoute.Main) }

    // These lists hold the data loaded from the Spring backend.
    // They start empty, then refresh() fills them.
    var sessions by remember { mutableStateOf<List<StudySessionResponse>>(emptyList()) }
    var goals by remember { mutableStateOf<List<GoalResponse>>(emptyList()) }
    var missions by remember { mutableStateOf<List<MissionResponse>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<ReviewReminderResponse>>(emptyList()) }
    var calendar by remember { mutableStateOf<List<CalendarDayResponse>>(emptyList()) }
    var progress by remember { mutableStateOf<ProgressResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        if (isGuest) {
            loading = false
            message = "Modo convidado: algumas funções precisam de login."
            return
        }
        loading = true
        message = null
        // Calls several backend endpoints and stores the results in screen state.
        scope.apiCall(
            action = {
                ScreenData(
                    sessions = api.studySessions(user.userId),
                    goals = api.goals(user.userId),
                    missions = api.missions(user.userId),
                    reviews = api.reviews(user.userId),
                    calendar = api.calendar(user.userId),
                    progress = api.progress(user.userId)
                )
            },
            onSuccess = {
                sessions = it.sessions
                goals = it.goals
                missions = it.missions
                reviews = it.reviews
                calendar = it.calendar
                progress = it.progress
                loading = false
            },
            onError = {
                message = it
                loading = false
            }
        )
    }

    LaunchedEffect(user.userId) {
        // Runs once when this user's HomeScreen appears.
        refresh()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedTab,
                items = tabs,
                onSelected = {
                    selectedTab = it
                    if (it == 0) {
                        homeRoute = HomeRoute.Main
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(99.dp)),
                    color = LearnerPrimary,
                    trackColor = LearnerPrimaryTint
                )
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            when (selectedTab) {
                0 -> when (homeRoute) {
                    HomeRoute.Main -> HomeMainScreen(
                        user = user,
                        progress = progress,
                        sessions = sessions,
                        onOpenPomodoro = { homeRoute = HomeRoute.Pomodoro },
                        onOpenNotifications = { homeRoute = HomeRoute.Notifications }
                    )
                    HomeRoute.Pomodoro -> PomodoroDetailScreen(
                        api = api,
                        userId = user.userId,
                        onBack = { homeRoute = HomeRoute.Main },
                        onDone = { refresh() }
                    )
                    HomeRoute.Notifications -> NotificationsScreen(
                        reviews = reviews,
                        onBack = { homeRoute = HomeRoute.Main }
                    )
                }
                1 -> ReportReviewsScreen(api, user.userId, sessions, reviews, onDone = { refresh() })
                2 -> CalendarScreen(calendar)
                3 -> StatsScreen(progress)
                4 -> GoalsMissionsScreen(api, goals, missions, onClaimed = {
                    onUserUpdated(user.copy(coins = user.coins + it))
                    refresh()
                })
            }
        }
    }
}

enum class HomeRoute {
    Main,
    Pomodoro,
    Notifications
}

@Composable
fun HomeMainScreen(
    user: AuthResponse,
    progress: ProgressResponse?,
    sessions: List<StudySessionResponse>,
    onOpenPomodoro: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    HeaderSection(
        userName = user.name,
        coins = user.coins,
        onNotificationClick = onOpenNotifications
    )
    ProgressOverview(progress = progress)

    HomeFeatureCard(
        title = "Pomodoro",
        subtitle = "Inicie uma sessão focada com contador",
        meta = "${sessions.size} registros salvos",
        accent = LearnerPrimary,
        onClick = onOpenPomodoro
    )
}

@Composable
fun ProgressOverview(progress: ProgressResponse?) {
    val totalMinutes = progress?.totalMinutes ?: 0
    val progressPercent = (totalMinutes / 120f).coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Seu estudo de hoje",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(LearnerPrimaryTint)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent.coerceAtLeast(0.08f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(LearnerTeal)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${(progressPercent * 100).toInt()}% do seu progresso",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(18.dp))
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(LearnerPrimaryTint),
            contentAlignment = Alignment.Center
        ) {
            Text("🙂", fontSize = 28.sp)
        }
    }

    StudyProgressCard(
        title = "Gramática básica",
        action = "Continuar",
        progress = "01/30",
        accent = Color(0xFF6A63FF),
        variant = 0
    )
    StudyProgressCard(
        title = "Conversa guiada",
        action = "Começar",
        progress = "01/30",
        accent = LearnerPrimary,
        variant = 1
    )
}

@Composable
fun HomeFeatureCard(
    title: String,
    subtitle: String,
    meta: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = accent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp)
                .padding(18.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = size.width * 0.24f,
                    center = Offset(size.width * 0.88f, size.height * 0.1f)
                )
                drawCircle(
                    color = Color(0xFF243BCE).copy(alpha = 0.16f),
                    radius = size.width * 0.20f,
                    center = Offset(size.width * 0.62f, size.height * 1.0f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.78f, size.height * 0.55f),
                    style = Stroke(width = 8f)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(size.width * 0.78f, size.height * 0.55f),
                    end = Offset(size.width * 0.78f, size.height * 0.38f),
                    strokeWidth = 5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(size.width * 0.78f, size.height * 0.55f),
                    end = Offset(size.width * 0.88f, size.height * 0.55f),
                    strokeWidth = 5f
                )
            }
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = meta,
                        color = accent,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StudyProgressCard(
    title: String,
    action: String,
    progress: String,
    accent: Color,
    variant: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent)
            .padding(20.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.width * 0.32f,
                center = Offset(size.width * 0.92f, size.height * 1.1f)
            )
            drawCircle(
                color = Color(0xFF243BCE).copy(alpha = 0.22f),
                radius = size.width * 0.18f,
                center = Offset(size.width * 0.58f, size.height * 0.98f)
            )
            if (variant == 0) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.36f),
                    topLeft = Offset(size.width * 0.58f, size.height * 0.3f),
                    size = Size(size.width * 0.18f, size.height * 0.42f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 8f)
                )
                drawRoundRect(
                    color = Color(0xFF201A73).copy(alpha = 0.58f),
                    topLeft = Offset(size.width * 0.72f, size.height * 0.5f),
                    size = Size(size.width * 0.18f, size.height * 0.42f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 8f)
                )
            } else {
                drawCircle(Color.White.copy(alpha = 0.24f), radius = 30f, center = Offset(size.width * 0.72f, size.height * 0.58f))
                drawCircle(Color.White.copy(alpha = 0.86f), radius = 11f, center = Offset(size.width * 0.72f, size.height * 0.52f))
                drawCircle(accent.copy(alpha = 0.85f), radius = 3.5f, center = Offset(size.width * 0.72f, size.height * 0.52f))
                drawLine(
                    color = Color(0xFF243BCE).copy(alpha = 0.7f),
                    start = Offset(size.width * 0.78f, size.height * 0.54f),
                    end = Offset(size.width * 1.02f, size.height * 0.2f),
                    strokeWidth = 5f
                )
                drawCircle(LearnerTeal.copy(alpha = 0.9f), radius = 9f, center = Offset(size.width * 0.82f, size.height * 0.72f))
                drawCircle(LearnerTeal.copy(alpha = 0.9f), radius = 8f, center = Offset(size.width * 0.85f, size.height * 0.38f))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 7.dp)
            ) {
                Text(
                    text = action,
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.28f))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
            Text(
                text = progress,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun HeaderSection(
    userName: String,
    coins: Int,
    onNotificationClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(LearnerPrimary, Color(0xFF5D7CFF))
                )
            )
            .padding(22.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.width * 0.28f,
                center = Offset(size.width * 0.06f, size.height * 0.02f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = size.width * 0.36f,
                center = Offset(size.width * 0.16f, size.height * 0.12f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Olá,",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                    Text(
                        text = "bom dia, ${userName.firstName()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White.copy(alpha = 0.92f),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "$coins moedas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .clickable(onClick = onNotificationClick),
                    contentAlignment = Alignment.Center
                ) {
                    NavGlyph(index = 2, color = Color.White)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 13.dp, end = 14.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(LearnerCoral)
                    )
                }
            }
            SearchBar()
        }
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchGlyph(color = LearnerMutedText.copy(alpha = 0.55f))
        Spacer(Modifier.width(14.dp))
        Text(
            text = "Buscar",
            modifier = Modifier.weight(1f),
            color = LearnerMutedText.copy(alpha = 0.68f),
            fontSize = 18.sp
        )
        Text(
            text = "Tudo",
            color = LearnerMutedText.copy(alpha = 0.78f),
            fontSize = 16.sp
        )
        Spacer(Modifier.width(8.dp))
        Canvas(modifier = Modifier.size(18.dp)) {
            drawLine(
                color = LearnerMutedText.copy(alpha = 0.65f),
                start = Offset(size.width * 0.2f, size.height * 0.35f),
                end = Offset(size.width * 0.5f, size.height * 0.65f),
                strokeWidth = 3f
            )
            drawLine(
                color = LearnerMutedText.copy(alpha = 0.65f),
                start = Offset(size.width * 0.8f, size.height * 0.35f),
                end = Offset(size.width * 0.5f, size.height * 0.65f),
                strokeWidth = 3f
            )
        }
    }
}


@Composable
fun BottomNavBar(
    selectedIndex: Int,
    items: List<String>,
    onSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .size(if (selected) 52.dp else 44.dp)
                        .clip(CircleShape)
                        .background(if (selected) LearnerPrimaryTint else Color.Transparent)
                        .clickable { onSelected(index) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NavGlyph(index = index, color = if (selected) LearnerPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SearchGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.42f, size.height * 0.42f),
            style = Stroke(width = 4f)
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.62f, size.height * 0.62f),
            end = Offset(size.width * 0.86f, size.height * 0.86f),
            strokeWidth = 4f
        )
    }
}

@Composable
fun NavGlyph(index: Int, color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 3.2f)
        when (index) {
            0 -> {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.48f), Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.82f, size.height * 0.48f), Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = 3.2f)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.28f, size.height * 0.45f),
                    size = Size(size.width * 0.44f, size.height * 0.36f),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = stroke
                )
            }
            1 -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.14f),
                    size = Size(size.width * 0.52f, size.height * 0.72f),
                    cornerRadius = CornerRadius(5f, 5f),
                    style = stroke
                )
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.34f), Offset(size.width * 0.66f, size.height * 0.34f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.5f), Offset(size.width * 0.66f, size.height * 0.5f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.66f), Offset(size.width * 0.54f, size.height * 0.66f), strokeWidth = 3.2f)
            }
            2 -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.18f),
                    size = Size(size.width * 0.56f, size.height * 0.6f),
                    cornerRadius = CornerRadius(5f, 5f),
                    style = stroke
                )
                drawLine(color, Offset(size.width * 0.22f, size.height * 0.38f), Offset(size.width * 0.78f, size.height * 0.38f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.36f, size.height * 0.1f), Offset(size.width * 0.36f, size.height * 0.26f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.64f, size.height * 0.1f), Offset(size.width * 0.64f, size.height * 0.26f), strokeWidth = 3.2f)
            }
            3 -> {
                drawLine(color, Offset(size.width * 0.2f, size.height * 0.8f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.3f, size.height * 0.68f), Offset(size.width * 0.3f, size.height * 0.8f), strokeWidth = 5f)
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.48f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = 5f)
                drawLine(color, Offset(size.width * 0.7f, size.height * 0.28f), Offset(size.width * 0.7f, size.height * 0.8f), strokeWidth = 5f)
            }
            4 -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    style = stroke
                )
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.12f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    style = stroke
                )
                drawLine(color, Offset(size.width * 0.62f, size.height * 0.38f), Offset(size.width * 0.82f, size.height * 0.18f), strokeWidth = 3.2f)
            }
            5 -> {
                drawArc(
                    color = color,
                    startAngle = 210f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.18f),
                    size = Size(size.width * 0.52f, size.height * 0.52f),
                    style = stroke
                )
                drawLine(color, Offset(size.width * 0.28f, size.height * 0.2f), Offset(size.width * 0.28f, size.height * 0.38f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.28f, size.height * 0.2f), Offset(size.width * 0.46f, size.height * 0.2f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.3f, size.height * 0.78f), Offset(size.width * 0.7f, size.height * 0.78f), strokeWidth = 3.2f)
            }
            else -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.3f),
                    size = Size(size.width * 0.56f, size.height * 0.46f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = stroke
                )
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.3f), Offset(size.width * 0.34f, size.height * 0.18f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.66f, size.height * 0.3f), Offset(size.width * 0.66f, size.height * 0.18f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.36f, size.height * 0.18f), Offset(size.width * 0.64f, size.height * 0.18f), strokeWidth = 3.2f)
            }
        }
    }
}

fun String.firstName(): String = trim().split(" ").firstOrNull().orEmpty().ifBlank { "estudante" }

