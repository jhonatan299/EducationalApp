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


