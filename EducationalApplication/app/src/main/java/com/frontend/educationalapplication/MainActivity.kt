package com.frontend.educationalapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.frontend.educationalapplication.network.AuthResponse
import com.frontend.educationalapplication.network.CalendarDayResponse
import com.frontend.educationalapplication.network.EducationalApi
import com.frontend.educationalapplication.network.GoalResponse
import com.frontend.educationalapplication.network.MissionResponse
import com.frontend.educationalapplication.network.ProgressResponse
import com.frontend.educationalapplication.network.ReviewReminderResponse
import com.frontend.educationalapplication.network.StudySessionResponse
import com.frontend.educationalapplication.ui.theme.EducationalApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EducationalApplicationTheme {
                StudyApp()
            }
        }
    }
}

@Composable
private fun StudyApp() {
    val api = remember { EducationalApi() }
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<AuthResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (user == null) {
            LoginScreen(
                error = error,
                onLogin = { email, password ->
                    error = null
                    scope.apiCall(
                        action = { api.login(email, password) },
                        onSuccess = { user = it },
                        onError = { error = it }
                    )
                }
            )
        } else {
            HomeScreen(api = api, user = user!!, onUserUpdated = { user = it })
        }
    }
}

@Composable
private fun LoginScreen(
    error: String?,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("demo@educational.app") }
    var password by remember { mutableStateOf("123456") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Educational App", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Study timer, goals, progress and rewards", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    api: EducationalApi,
    user: AuthResponse,
    onUserUpdated: (AuthResponse) -> Unit
) {
    val tabs = listOf("Pomodoro", "Report", "Calendar", "Stats", "Goals", "Reviews", "Missions")
    var selectedTab by remember { mutableIntStateOf(0) }
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
        loading = true
        message = null
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
        refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(user.name, fontWeight = FontWeight.Bold)
                        Text("${user.coins} coins", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    TextButton(onClick = { refresh() }) {
                        Text("Refresh")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = { Text(title.take(1)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            when (selectedTab) {
                0 -> PomodoroScreen(api, user.userId, onDone = { refresh() })
                1 -> HistoryScreen(api, user.userId, sessions, onDone = { refresh() })
                2 -> CalendarScreen(calendar)
                3 -> StatsScreen(progress)
                4 -> GoalsScreen(goals)
                5 -> ReviewsScreen(api, reviews, onDone = { refresh() })
                6 -> MissionsScreen(api, missions, onClaimed = {
                    onUserUpdated(user.copy(coins = user.coins + it))
                    refresh()
                })
            }
        }
    }
}

@Composable
private fun PomodoroScreen(api: EducationalApi, userId: Long, onDone: () -> Unit) {
    var subject by remember { mutableStateOf("Matematica") }
    var focusMinutes by remember { mutableStateOf("25") }
    var breakMinutes by remember { mutableStateOf("5") }
    var pomodoroId by remember { mutableLongStateOf(0L) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(running) {
        while (running && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        }
        if (running && remainingSeconds == 0 && pomodoroId != 0L) {
            running = false
            status = "Focus complete"
        }
    }

    SectionCard("Pomodoro") {
        Text(timerText(remainingSeconds), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text(status)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField("Focus", focusMinutes, { focusMinutes = it }, Modifier.weight(1f))
            NumberField("Break", breakMinutes, { breakMinutes = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val focus = focusMinutes.toIntOrNull() ?: 25
                    val pause = breakMinutes.toIntOrNull() ?: 5
                    scope.apiCall(
                        action = { api.startPomodoro(userId, subject, focus, pause) },
                        onSuccess = {
                            pomodoroId = it.id
                            remainingSeconds = it.totalFocusMinutes * 60
                            running = true
                            status = "Running"
                        },
                        onError = { status = it }
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Start")
            }
            Button(onClick = { running = !running }, modifier = Modifier.weight(1f)) {
                Text(if (running) "Pause" else "Resume")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (pomodoroId != 0L) {
                        scope.apiCall(
                            action = { api.extendPomodoro(pomodoroId) },
                            onSuccess = {
                                remainingSeconds += 5 * 60
                                status = "+5 minutes"
                            },
                            onError = { status = it }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("+5")
            }
            Button(
                onClick = {
                    if (pomodoroId != 0L) {
                        scope.apiCall(
                            action = { api.completePomodoro(pomodoroId, "Completed in Android prototype") },
                            onSuccess = {
                                running = false
                                status = "Saved"
                                onDone()
                            },
                            onError = { status = it }
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Complete")
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    api: EducationalApi,
    userId: Long,
    sessions: List<StudySessionResponse>,
    onDone: () -> Unit
) {
    var subject by remember { mutableStateOf("Historia") }
    var minutes by remember { mutableStateOf("30") }
    var studyType by remember { mutableStateOf("THEORY") }
    var content by remember { mutableStateOf("Brasil Republica") }
    var objective by remember { mutableStateOf("Entender o tema da aula") }
    var notes by remember { mutableStateOf("") }
    var learned by remember { mutableStateOf("") }
    var difficulties by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }
    var focusRating by remember { mutableStateOf("4") }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    SectionCard("Session Report") {
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") })
        NumberField("Minutes", minutes, { minutes = it }, Modifier.fillMaxWidth())
        OutlinedTextField(value = studyType, onValueChange = { studyType = it.uppercase() }, label = { Text("Study type") })
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") })
        OutlinedTextField(value = objective, onValueChange = { objective = it }, label = { Text("Objective before studying") })
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Quick notes") })
        OutlinedTextField(value = learned, onValueChange = { learned = it }, label = { Text("What I learned") })
        OutlinedTextField(value = difficulties, onValueChange = { difficulties = it }, label = { Text("Difficulties") })
        OutlinedTextField(value = nextAction, onValueChange = { nextAction = it }, label = { Text("Next action") })
        NumberField("Focus rating 1-5", focusRating, { focusRating = it.take(1) }, Modifier.fillMaxWidth())
        Button(
            onClick = {
                scope.apiCall(
                    action = {
                        api.createManualSession(
                            userId = userId,
                            subject = subject,
                            durationMinutes = minutes.toIntOrNull() ?: 1,
                            studyType = studyType.ifBlank { "THEORY" },
                            content = content,
                            objective = objective,
                            notes = notes,
                            learned = learned,
                            difficulties = difficulties,
                            nextAction = nextAction,
                            focusRating = (focusRating.toIntOrNull() ?: 3).coerceIn(1, 5)
                        )
                    },
                    onSuccess = {
                        status = "Session report saved. Review reminder created."
                        onDone()
                    },
                    onError = { status = it }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save session")
        }
        status?.let { Text(it) }
    }

    SectionCard("History") {
        sessions.forEach { session ->
            ListLine(
                title = "${session.subject} - ${session.content ?: session.studyType ?: session.source}",
                detail = "${session.durationMinutes} min - focus ${session.focusRating ?: "-"} - next: ${session.nextAction ?: "none"}"
            )
        }
    }
}

@Composable
private fun CalendarScreen(calendar: List<CalendarDayResponse>) {
    SectionCard("Calendar") {
        if (calendar.isEmpty()) {
            Text("No sessions this month yet")
            return@SectionCard
        }
        calendar.forEach { day ->
            Text("${day.date} - ${day.totalMinutes} min", fontWeight = FontWeight.Bold)
            day.subjects.forEach {
                Text("${it.subject}: ${it.minutes} min", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatsScreen(progress: ProgressResponse?) {
    SectionCard("Progress") {
        if (progress == null) {
            Text("No data yet")
            return@SectionCard
        }
        Text("${progress.totalMinutes} minutes studied this week", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("By subject")
        progress.bySubject.forEach { item ->
            BarLine(label = item.subject, value = item.minutes, max = progress.totalMinutes.coerceAtLeast(1))
        }
        Spacer(Modifier.height(8.dp))
        Text("By day")
        progress.byDay.forEach { item ->
            BarLine(label = item.date.takeLast(5), value = item.minutes, max = progress.totalMinutes.coerceAtLeast(1))
        }
    }
}

@Composable
private fun GoalsScreen(goals: List<GoalResponse>) {
    SectionCard("Goals") {
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
private fun MissionsScreen(
    api: EducationalApi,
    missions: List<MissionResponse>,
    onClaimed: (Int) -> Unit
) {
    SectionCard("Missions") {
        val scope = rememberCoroutineScope()
        missions.forEach { mission ->
            ListLine(
                title = mission.title,
                detail = "${mission.progressMinutes}/${mission.targetMinutes} min - ${mission.rewardCoins} coins"
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
                Text(if (mission.claimed) "Claimed" else "Claim")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReviewsScreen(
    api: EducationalApi,
    reviews: List<ReviewReminderResponse>,
    onDone: () -> Unit
) {
    SectionCard("Review Alerts") {
        val scope = rememberCoroutineScope()
        if (reviews.isEmpty()) {
            Text("No review reminders yet")
            return@SectionCard
        }
        reviews.forEach { review ->
            ListLine(
                title = review.title,
                detail = "${review.subject} - due ${review.dueDate}"
            )
            Button(
                enabled = !review.completed,
                onClick = {
                    scope.apiCall(
                        action = { api.completeReview(review.id) },
                        onSuccess = { onDone() },
                        onError = {}
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (review.completed) "Reviewed" else "Mark reviewed")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun NumberField(
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
private fun ListLine(title: String, detail: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(detail, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BarLine(label: String, value: Int, max: Int) {
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

private fun timerText(seconds: Int): String {
    val minutes = seconds / 60
    val rest = seconds % 60
    return "%02d:%02d".format(minutes, rest)
}

private fun <T> CoroutineScope.apiCall(
    action: suspend () -> T,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
) {
    launch {
        runCatching { action() }
            .onSuccess(onSuccess)
            .onFailure { onError(it.message ?: "Request failed") }
    }
}

private data class ScreenData(
    val sessions: List<StudySessionResponse>,
    val goals: List<GoalResponse>,
    val missions: List<MissionResponse>,
    val reviews: List<ReviewReminderResponse>,
    val calendar: List<CalendarDayResponse>,
    val progress: ProgressResponse
)
