package com.frontend.educationalapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
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
fun HomeScreen(
    api: EducationalApi,
    user: AuthResponse,
    isGuest: Boolean,
    isInPictureInPictureMode: Boolean = false,
    onEnterPictureInPicture: () -> Unit = {},
    onUserUpdated: (AuthResponse) -> Unit,
    onLogout: () -> Unit
) {
    val tabs = listOf("Home", "Relatório", "Calendário", "Estatísticas", "Metas", "Jogos")
    var selectedTab by remember { mutableIntStateOf(0) }
    var homeRoute by remember { mutableStateOf(HomeRoute.Main) }
    var selectedLearningPath by remember { mutableStateOf("Matematica aplicada") }
    var reportInitialSection by remember { mutableStateOf<Int?>(null) }

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
    val context = LocalContext.current
    val pomodoroPlayerState = rememberPomodoroPlayerState()
    var blockOtherAppsDuringPomodoro by remember {
        mutableStateOf(isBlockOtherAppsDuringPomodoroEnabled(context))
    }

    PomodoroTicker(pomodoroPlayerState)
    PomodoroDoNotDisturbEffect(
        context = context,
        enabled = blockOtherAppsDuringPomodoro,
        pomodoroRunning = pomodoroPlayerState.running
    )

    LaunchedEffect(pomodoroPlayerState.showQuickReport) {
        if (pomodoroPlayerState.showQuickReport) {
            selectedTab = 0
            homeRoute = HomeRoute.Pomodoro
        }
    }

    if (isInPictureInPictureMode) {
        PomodoroPictureInPictureContent(pomodoroPlayerState)
        return
    }

    fun refresh() {
        if (isGuest) {
            val demo = demoScreenData(user.userId)
            sessions = demo.sessions
            goals = demo.goals
            missions = demo.missions
            reviews = demo.reviews
            calendar = demo.calendar
            progress = demo.progress
            loading = false
            message = "Modo demo: dados de exemplo para apresenta??o."
            return
        }
        loading = true
        message = null
        // Calls several backend endpoints and stores the results in screen state.
        scope.apiCall(
            action = {
                val synced = syncPendingStudySessions(context, api, user.userId)
                ScreenData(
                    sessions = api.studySessions(user.userId),
                    goals = api.goals(user.userId),
                    missions = api.missions(user.userId),
                    reviews = api.reviews(user.userId),
                    calendar = api.calendar(user.userId),
                    progress = api.progress(user.userId)
                ) to synced
            },
            onSuccess = {
                val data = it.first
                sessions = data.sessions
                goals = data.goals
                missions = data.missions
                reviews = data.reviews
                calendar = data.calendar
                progress = data.progress
                cacheScreenData(context, user.userId, data)
                message = if (it.second > 0) "${it.second} sessão offline sincronizada." else null
                loading = false
            },
            onError = {
                val cached = loadCachedScreenData(context, user.userId)
                if (cached != null) {
                    sessions = cached.sessions
                    goals = cached.goals
                    missions = cached.missions
                    reviews = cached.reviews
                    calendar = cached.calendar
                    progress = cached.progress
                    message = "Offline: mostrando os ultimos dados salvos neste aparelho."
                } else {
                    message = it
                }
                loading = false
            }
        )
    }

    LaunchedEffect(user.userId) {
        // Runs once when this user's HomeScreen appears.
        refresh()
    }

    if (homeRoute == HomeRoute.QuizPoker) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            QuizPokerMainScreen(onBack = { homeRoute = HomeRoute.Main })
        }
    } else if (homeRoute == HomeRoute.PsychBluff) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            PsychBluffMainScreen(onBack = { homeRoute = HomeRoute.Main })
        }
    } else if (homeRoute == HomeRoute.AiQuizGenerator) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(20.dp)
        ) {
            AiQuizGeneratorScreen(api = api, onBack = { homeRoute = HomeRoute.Main })
        }
    } else if (homeRoute == HomeRoute.QuestionBank) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            QuestionBankScreen(api = api, onBack = { homeRoute = HomeRoute.Main })
        }
    } else if (homeRoute == HomeRoute.QuizSolo) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            QuizSoloScreen(
                api = api,
                userId = user.userId,
                onBack = { homeRoute = HomeRoute.Main },
                onCoinsBalanceUpdated = { coins -> onUserUpdated(user.copy(coins = coins)) }
            )
        }
    } else if (homeRoute == HomeRoute.Ranking) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RankingDashboard(api = api, userId = user.userId, onBack = { homeRoute = HomeRoute.Main })
        }
    } else if (homeRoute == HomeRoute.WeeklyProgress) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                BottomNavBar(
                    selectedIndex = selectedTab,
                    items = tabs,
                    onSelected = {
                        selectedTab = it
                        homeRoute = HomeRoute.Main
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                WeeklyProgressTasksScreen(
                    calendar = calendar,
                    reviews = reviews,
                    goals = goals,
                    onBack = { homeRoute = HomeRoute.Main }
                )
            }
        }
    } else if (homeRoute == HomeRoute.ChangePassword) {
        ChangePasswordScreen(
            onBack = { homeRoute = HomeRoute.Profile }
        )
    } else {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                selectedIndex = selectedTab,
                items = tabs,
                onSelected = {
                    if (it == 1) reportInitialSection = null
                    selectedTab = it
                    if (it == 0) {
                        homeRoute = HomeRoute.Main
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
        Column(
            modifier = Modifier
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
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            message?.let {
                if (it.startsWith("Modo convidado") || it.startsWith("Modo demo") || it.startsWith("Offline:") || it.contains("sincronizada")) {
                    Text(it, color = LearnerMutedText)
                } else {
                    ErrorStateCard(
                        error = it.toFriendlyError(),
                        onRetry = { refresh() },
                        onGoHome = {
                            selectedTab = 0
                            homeRoute = HomeRoute.Main
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> when (homeRoute) {
                    HomeRoute.Main -> HomeMainScreen(
                        user = user,
                        progress = progress,
                        sessions = sessions,
                        goals = goals,
                        onOpenPomodoro = { homeRoute = HomeRoute.Pomodoro },
                        reviews = reviews,
                        onOpenProfile = { homeRoute = HomeRoute.Profile },
                        onOpenHelp = { homeRoute = HomeRoute.Help },
                        onOpenPriorityReview = {
                            reportInitialSection = 1
                            selectedTab = 1
                            homeRoute = HomeRoute.Main
                        },
                        onOpenBestGoal = {
                            selectedTab = 4
                            homeRoute = HomeRoute.Main
                        },
                        onOpenWeeklyProgress = { homeRoute = HomeRoute.WeeklyProgress },
                        onOpenQuizPoker = { homeRoute = HomeRoute.QuizPoker }
                    )
                    HomeRoute.Pomodoro -> PomodoroDetailScreen(
                        api = api,
                        userId = user.userId,
                        playerState = pomodoroPlayerState,
                        onBack = { homeRoute = HomeRoute.Main },
                        onDone = { refresh() },
                        onMinimize = onEnterPictureInPicture
                    )
                    HomeRoute.Profile -> ProfileScreen(
                        api = api,
                        user = user,
                        reviews = reviews,
                        blockOtherAppsDuringPomodoro = blockOtherAppsDuringPomodoro,
                        onBlockOtherAppsDuringPomodoroChanged = {
                            blockOtherAppsDuringPomodoro = it
                            setBlockOtherAppsDuringPomodoroEnabled(context, it)
                        },
                        onUserUpdated = onUserUpdated,
                        onChangePassword = { homeRoute = HomeRoute.ChangePassword },
                        onLogout = onLogout,
                        onBack = { homeRoute = HomeRoute.Main }
                    )
                    HomeRoute.Help -> HelpScreen(onBack = { homeRoute = HomeRoute.Main })
                    HomeRoute.LearningPath -> LearningPathScreen(
                        title = selectedLearningPath,
                        onBack = { homeRoute = HomeRoute.Main },
                        onStartFocus = { homeRoute = HomeRoute.Pomodoro }
                    )
                    HomeRoute.WeeklyProgress -> Unit
                    HomeRoute.QuizPoker -> Unit
                    HomeRoute.PsychBluff -> Unit
                    HomeRoute.AiQuizGenerator -> Unit
                    HomeRoute.QuestionBank -> Unit
                    HomeRoute.QuizSolo -> Unit
                    HomeRoute.Ranking -> Unit
                    HomeRoute.ChangePassword -> Unit
                }
                1 -> ReportReviewsScreen(
                    api,
                    user.userId,
                    sessions,
                    reviews,
                    onDone = { refresh() },
                    onStartStudy = {
                        selectedTab = 0
                        homeRoute = HomeRoute.Pomodoro
                    },
                    initialSection = reportInitialSection
                )
                2 -> CalendarScreen(calendar)
                3 -> ProgressDashboard(progress, goals, onOpenRanking = { homeRoute = HomeRoute.Ranking })
                4 -> GoalsMissionsDashboard(api, user.userId, sessions, goals, missions, onClaimed = {
                    onUserUpdated(user.copy(coins = user.coins + it))
                    refresh()
                })
                5 -> GamesHubScreen(
                    onOpenQuizSolo = { homeRoute = HomeRoute.QuizSolo },
                    onOpenQuizPoker = { homeRoute = HomeRoute.QuizPoker },
                    onOpenPsychBluff = { homeRoute = HomeRoute.PsychBluff },
                    onOpenAiQuizGenerator = { homeRoute = HomeRoute.AiQuizGenerator },
                    onOpenQuestionBank = { homeRoute = HomeRoute.QuestionBank }
                )
            }
        }
        }
    }
}
}

@Composable
fun GamesHubScreen(
    onOpenQuizSolo: () -> Unit,
    onOpenQuizPoker: () -> Unit,
    onOpenPsychBluff: () -> Unit,
    onOpenAiQuizGenerator: () -> Unit,
    onOpenQuestionBank: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Jogos", fontFamily = CircularDisplayFamily, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Desafie amigos e aprenda jogando.", color = LearnerMutedText)
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenQuizSolo),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(LearnerPrimaryTint, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("1P", fontSize = 24.sp, color = LearnerPrimary, fontWeight = FontWeight.Black) }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quiz Solo", color = Color(0xFF111827), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Responda perguntas, teste seus conhecimentos e ganhe pontos.", color = LearnerMutedText, fontSize = 13.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(LearnerPrimary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Jogar agora", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Text(">", color = LearnerPrimary, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenAiQuizGenerator),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF5B4BFF)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(Color.White.copy(alpha = .2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("AI", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black) }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gerar quiz com IA", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Cole texto e crie perguntas para Poker ou Psych", color = Color.White.copy(alpha = .78f), fontSize = 13.sp)
                }
                Text(">", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenQuizPoker),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF123D25)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("P", fontSize = 30.sp, color = Color(0xFF123D25), fontWeight = FontWeight.Black) }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quiz Poker", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Trivia entre amigos com fichas e apostas", color = Color.White.copy(alpha = .76f), fontSize = 13.sp)
                }
                Text(">", color = Color(0xFFFFD700), fontSize = 32.sp)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenQuestionBank),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(LearnerPrimaryTint, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("Q", fontSize = 26.sp, color = LearnerPrimary, fontWeight = FontWeight.Black) }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Banco de perguntas", color = Color(0xFF111827), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Buscar, reclassificar e eliminar perguntas salvas", color = LearnerMutedText, fontSize = 13.sp)
                }
                Text(">", color = LearnerPrimary, fontSize = 28.sp, fontWeight = FontWeight.Black)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenPsychBluff),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1054)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(Color(0xFFFFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("?", fontSize = 30.sp, color = Color(0xFF2D1054), fontWeight = FontWeight.Black) }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Psych! Bluff Trivia", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("Invente respostas e engane seus amigos", color = Color.White.copy(alpha = .76f), fontSize = 13.sp)
                }
                Text(">", color = Color(0xFFFFD700), fontSize = 32.sp)
            }
        }
        Text("Mais dinamicas em breve", color = LearnerMutedText, fontWeight = FontWeight.Bold)
        Text("Este espaco fica pronto para os proximos jogos entre amigos.", color = LearnerMutedText)
    }
}

enum class HomeRoute {
    Main,
    Pomodoro,
    Profile,
    Help,
    LearningPath,
    QuizPoker,
    PsychBluff,
    AiQuizGenerator,
    QuestionBank,
    QuizSolo,
    Ranking,
    WeeklyProgress,
    ChangePassword
}

@Composable
fun HomeMainScreen(
    user: AuthResponse,
    progress: ProgressResponse?,
    sessions: List<StudySessionResponse>,
    goals: List<GoalResponse>,
    reviews: List<ReviewReminderResponse>,
    onOpenPomodoro: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenPriorityReview: () -> Unit,
    onOpenBestGoal: () -> Unit,
    onOpenWeeklyProgress: () -> Unit,
    onOpenQuizPoker: () -> Unit
) {
    HeaderSection(
        userName = user.name,
        coins = user.coins,
        hasNotifications = reviews.any { !it.completed },
        onProfileClick = onOpenProfile,
        onHelpClick = onOpenHelp
    )

    ProgressOverview(progress = progress, sessions = sessions, goals = goals, onOpenWeeklyProgress = onOpenWeeklyProgress)
    HomeFeatureCard(
        title = "Pomodoro",
        subtitle = "Inicie uma sessão focada com contador",
        meta = "${sessions.size} registros salvos",
        accent = LearnerPrimary,
        onClick = onOpenPomodoro
    )
    PriorityActionCards(
        reviews = reviews,
        goals = goals,
        onOpenPriorityReview = onOpenPriorityReview,
        onOpenBestGoal = onOpenBestGoal
    )
}

@Composable
fun ProgressOverview(
    progress: ProgressResponse?,
    sessions: List<StudySessionResponse>,
    goals: List<GoalResponse>,
    onOpenWeeklyProgress: () -> Unit
) {
    val primaryGoal = goals.firstOrNull()
    val goalProgress = ((primaryGoal?.progressPercent ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
    val weeklyTargetMinutes = 300
    val weeklyMinutes = progress?.totalMinutes ?: 0
    val weeklyProgress = (weeklyMinutes.toFloat() / weeklyTargetMinutes).coerceIn(0f, 1f)
    val displayProgress = if (primaryGoal != null) goalProgress else weeklyProgress
    val percent = (displayProgress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenWeeklyProgress() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Seu progresso de estudo",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF0F2A56),
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFE8EEFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(displayProgress.coerceAtLeast(0.02f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFF2F6BFF))
                    )
                }
                Text(
                    text = primaryGoal?.let {
                        "${it.currentMinutes}/${it.targetMinutes} min na meta atual - $percent%"
                    } ?: "$weeklyMinutes/$weeklyTargetMinutes min nesta semana",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF35507A)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEAF0FF))
                        .clickable { onOpenWeeklyProgress() }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Text("Ver semana", color = Color(0xFF315DFF), fontWeight = FontWeight.Black)
                }
            }
            WeeklyProgressRing(
                progress = displayProgress,
                label = "$percent%",
                onClick = onOpenWeeklyProgress
            )
        }
    }
}

@Composable
private fun WeeklyProgressRing(progress: Float, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(CircleShape)
            .background(Color(0xFFEAF0FF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(68.dp)) {
            drawCircle(
                color = Color(0xFFD7E0FF),
                radius = size.minDimension * .42f,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFF315DFF),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(size.width * .08f, size.height * .08f),
                size = Size(size.width * .84f, size.height * .84f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        }
        Text(
            text = label,
            color = Color(0xFF315DFF),
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun WeeklyProgressTasksScreen(
    calendar: List<CalendarDayResponse>,
    reviews: List<ReviewReminderResponse>,
    goals: List<GoalResponse>,
    onBack: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    var weekOffset by remember { mutableIntStateOf(0) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val today = remember { Calendar.getInstance() }
    val weekStart = remember(weekOffset) {
        (today.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }
    val weekDays = remember(weekOffset) {
        List(7) { index ->
            (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, index) }
        }
    }
    val selectedDate = dateFormat.format(weekDays[selectedIndex].time)
    val selectedCalendarDay = calendar.firstOrNull { it.date == selectedDate }
    val dayStudyItems = selectedCalendarDay?.subjects.orEmpty()
    val dayReviews = reviews.filter { it.dueDate == selectedDate }
    val dayGoals = goals.filter { it.dueDate == selectedDate }
    val dayCards = buildList {
        dayStudyItems.forEach {
            add(WeeklyTaskCardData("Estudo registrado", it.subject, "${it.minutes} min", "CONCLUIDA", LearnerPrimary, priority = false, completed = true))
        }
        dayReviews.forEach {
            add(WeeklyTaskCardData("Revisao", it.title, it.subject, if (it.completed) "CONCLUIDA" else "PRIORIDADE", LearnerCoral, priority = !it.completed, completed = it.completed))
        }
        dayGoals.forEach {
            add(WeeklyTaskCardData("Meta da semana", it.title, "${it.currentMinutes}/${it.targetMinutes} min", if (it.completed) "CONCLUIDA" else "PRIORIDADE", LearnerTeal, priority = !it.completed, completed = it.completed))
        }
    }
    val filteredCards = when (selectedFilter) {
        "PRIORIDADE" -> dayCards.filter { it.priority }
        "CONCLUIDAS" -> dayCards.filter { it.completed }
        else -> dayCards
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Semana de estudos", fontFamily = CircularDisplayFamily, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DayNavButton("<") {
                if (selectedIndex == 0) {
                    weekOffset -= 1
                    selectedIndex = 6
                } else {
                    selectedIndex -= 1
                }
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEachIndexed { index, day ->
                    val selected = index == selectedIndex
                    WeeklyDayChip(
                        month = monthPt(day.get(Calendar.MONTH)),
                        day = day.get(Calendar.DAY_OF_MONTH).toString(),
                        name = dayNamePt(day.get(Calendar.DAY_OF_WEEK)),
                        selected = selected,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedIndex = index }
                    )
                }
            }
            DayNavButton(">") {
                if (selectedIndex == 6) {
                    weekOffset += 1
                    selectedIndex = 0
                } else {
                    selectedIndex += 1
                }
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("TODOS", "PRIORIDADE", "CONCLUIDAS").forEach { filter ->
                WeeklyFilterChip(
                    text = filter,
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter }
                )
            }
        }

        if (filteredCards.isEmpty()) {
            EmptyStateCard(
                title = "Nada marcado para este dia",
                subtitle = "Use as setas para navegar entre os dias ou registre uma sessão de estudo."
            )
        } else {
            filteredCards.forEach { item -> WeeklyTaskCard(item) }
        }

        BottomBackButton(onBack = onBack)
    }
}

private data class WeeklyTaskCardData(
    val eyebrow: String,
    val title: String,
    val time: String,
    val status: String,
    val accent: Color,
    val priority: Boolean,
    val completed: Boolean
)

@Composable
private fun WeeklyDayChip(month: String, day: String, name: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) LearnerPrimary else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(month, color = if (selected) Color.White.copy(alpha = .85f) else LearnerMutedText, fontSize = 9.sp, maxLines = 1)
            Text(day, color = if (selected) Color.White else Color(0xFF111827), fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(name, color = if (selected) Color.White.copy(alpha = .9f) else LearnerMutedText, fontSize = 9.sp, maxLines = 1)
        }
    }
}

private fun monthPt(month: Int): String = listOf(
    "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
)[month.coerceIn(0, 11)]

private fun dayNamePt(dayOfWeek: Int): String = when (dayOfWeek) {
    Calendar.MONDAY -> "Seg"
    Calendar.TUESDAY -> "Ter"
    Calendar.WEDNESDAY -> "Qua"
    Calendar.THURSDAY -> "Qui"
    Calendar.FRIDAY -> "Sex"
    Calendar.SATURDAY -> "Sab"
    Calendar.SUNDAY -> "Dom"
    else -> ""
}

@Composable
private fun DayNavButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = LearnerPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun WeeklyFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) LearnerPrimary else Color(0xFFEDEBFA))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else LearnerPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeeklyTaskCard(item: WeeklyTaskCardData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.eyebrow, color = LearnerMutedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(item.title, color = Color(0xFF111827), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(item.time, color = item.accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(item.accent.copy(alpha = .14f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(item.status, color = item.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PriorityActionCards(
    reviews: List<ReviewReminderResponse>,
    goals: List<GoalResponse>,
    onOpenPriorityReview: () -> Unit,
    onOpenBestGoal: () -> Unit
) {
    val priorityReview = reviews
        .filter { !it.completed }
        .sortedWith(compareBy<ReviewReminderResponse> { it.dueDate }.thenBy { it.subject })
        .firstOrNull()
    val bestGoal = goals
        .filter { !it.completed }
        .maxByOrNull { it.progressPercent }

    StudyProgressCard(
        title = priorityReview?.let { it.topic.orEmpty().ifBlank { it.subject.ifBlank { it.title } } } ?: "Pr?xima revis?o",
        action = "Revisar",
        progress = priorityReview?.dueDate?.takeIf { it.isNotBlank() } ?: "${reviews.count { !it.completed }} pend.",
        accent = Color(0xFF6A63FF),
        variant = 0,
        onClick = onOpenPriorityReview
    )
    StudyProgressCard(
        title = bestGoal?.title ?: "Meta em progresso",
        action = "Continuar",
        progress = bestGoal?.let { "${it.currentMinutes}/${it.targetMinutes}" } ?: "0/0",
        progressFraction = bestGoal?.let { (it.progressPercent / 100.0).toFloat() },
        accent = Color(0xFFE88CB9),
        variant = 1,
        onClick = onOpenBestGoal
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
                .height(142.dp)
                .padding(20.dp)
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
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = CircularDisplayFamily
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.94f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CircularDisplayFamily
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
    progressFraction: Float? = null,
    accent: Color,
    variant: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp)
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

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = CircularDisplayFamily,
                    maxLines = 2
                )
            }
            ProgressPill(
                text = progress,
                modifier = Modifier.padding(top = if (progressFraction != null) 46.dp else 0.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = if (progressFraction != null) 58.dp else 54.dp)
                .fillMaxWidth(0.62f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            progressFraction?.let {
                LinearProgressIndicator(
                    progress = { it.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.28f)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White)
                    .padding(horizontal = 18.dp, vertical = 7.dp)
            ) {
                Text(
                    text = action,
                    color = accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CircularDisplayFamily
                )
            }
        }
    }
}

@Composable
private fun ProgressPill(text: String, modifier: Modifier = Modifier, subtle: Boolean = false) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White.copy(alpha = if (subtle) 0.18f else 0.28f))
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = CircularDisplayFamily
        )
    }
}


@Composable
fun HeaderSection(
    userName: String,
    coins: Int,
    hasNotifications: Boolean,
    onProfileClick: () -> Unit,
    onHelpClick: () -> Unit
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
                        text = "Ola, ${userName.firstName()}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontFamily = CircularDisplayFamily
                    )
                    if (false) {
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
                        text = "$coins pontos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onHelpClick),
                        contentAlignment = Alignment.Center
                    ) {
                        HelpGlyph(color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable(onClick = onProfileClick),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileGlyph(color = Color.White)
                        if (hasNotifications) {
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
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchGlyph(color = LearnerMutedText.copy(alpha = 0.78f))
        Spacer(Modifier.width(14.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = LearnerMutedText,
                fontSize = 18.sp
            ),
            cursorBrush = SolidColor(LearnerPrimary),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text("Buscar", color = LearnerMutedText.copy(alpha = 0.84f), fontSize = 18.sp)
                }
                innerTextField()
            }
        )
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
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
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
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.12f, size.height * 0.32f),
                    size = Size(size.width * 0.76f, size.height * 0.42f),
                    cornerRadius = CornerRadius(9f, 9f),
                    style = stroke
                )
                drawLine(color, Offset(size.width * 0.29f, size.height * 0.53f), Offset(size.width * 0.49f, size.height * 0.53f), strokeWidth = 3.2f)
                drawLine(color, Offset(size.width * 0.39f, size.height * 0.43f), Offset(size.width * 0.39f, size.height * 0.63f), strokeWidth = 3.2f)
                drawCircle(color, size.minDimension * 0.055f, Offset(size.width * 0.68f, size.height * 0.48f))
                drawCircle(color, size.minDimension * 0.055f, Offset(size.width * 0.77f, size.height * 0.58f))
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

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReportSubscreenHeader(title = "Ajuda", onBack = onBack)
        HelpTopicCard(
            title = "Como usar o Pomodoro",
            subtitle = "Escolha uma materia e um tempo de foco. Estude sem interrupcoes ate o contador terminar, faca a pausa e repita. Sessoes curtas ajudam a reduzir cansaco e facilitam comecar.",
            accent = LearnerPrimary
        )
        HelpTopicCard(
            title = "Beneficios do foco em blocos",
            subtitle = "Dividir o estudo em ciclos torna a tarefa mais leve, cria pausas reais e ajuda voc? a acompanhar quanto tempo dedicou a cada materia.",
            accent = LearnerTeal
        )
        HelpTopicCard(
            title = "Como funciona o engajamento",
            subtitle = "Sessoes concluidas alimentam seu historico, metas e missoes. Quando uma missao estiver completa, voc? pode resgatar pontos e acompanhar sua evolucao ao longo da semana.",
            accent = LearnerCoral
        )
        BottomBackButton(onBack = onBack)
    }
}

@Composable
fun LearningPathScreen(title: String, onBack: () -> Unit, onStartFocus: () -> Unit) {
    val isEngineering = title == "Fundamentos de engenharia"
    val firstActivity = if (isEngineering) "Estatica e fisica" else "Calculo e algebra"
    val secondActivity = if (isEngineering) "Analisar estruturas simples" else "Resolver problemas aplicados"
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReportSubscreenHeader(title = title, onBack = onBack)
        SectionCard("Proxima atividade") {
            Text(
                "Escolha uma atividade curta e use o Pomodoro para registrar seu foco.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ListLine(firstActivity, "15 min de estudo guiado")
            ListLine(secondActivity, "Aplicar o que foi estudado")
            Button(onClick = onStartFocus, modifier = Modifier.fillMaxWidth()) {
                Text("Iniciar sessão de foco")
            }
        }
        BottomBackButton(onBack = onBack)
    }
}

@Composable
fun HelpTopicCard(title: String, subtitle: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun HelpGlyph(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.38f,
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = 3f)
        )
        drawArc(
            color = color,
            startAngle = 220f,
            sweepAngle = 200f,
            useCenter = false,
            topLeft = Offset(size.width * 0.32f, size.height * 0.2f),
            size = Size(size.width * 0.36f, size.height * 0.42f),
            style = Stroke(width = 3f)
        )
        drawCircle(color = color, radius = 2.2f, center = Offset(size.width * 0.5f, size.height * 0.74f))
    }
}

fun String.firstName(): String = trim().split(" ").firstOrNull().orEmpty().ifBlank { "estudante" }

