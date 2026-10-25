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
import com.frontend.educationalapplication.ui.theme.LearnerText
@Composable
fun NotificationsScreen(
    reviews: List<ReviewReminderResponse>,
    onBack: () -> Unit
) {
    val pendingReviews = reviews.filter { !it.completed }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ReportSubscreenHeader(
            title = "Notificações",
            onBack = onBack
        )

        if (pendingReviews.isEmpty()) {
            EmptyNotificationsContent()
        } else {
            pendingReviews.forEach { review ->
                NotificationItemCard(
                    title = review.title,
                    subtitle = "Revisão pendente • ${review.subject}",
                    accent = LearnerPrimary
                )
            }
        }
        BottomBackButton(onBack = onBack)
    }
}

@Composable
fun NotificationTabs() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "mensagens",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "notificações",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
            )
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(LearnerPrimary)
            )
        }
    }
}

@Composable
fun EmptyNotificationsContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EmptyNotificationIllustration()
        Text(
            text = "Sem notificações ainda!",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "A gente avisa quando tiver algo novo para você.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyNotificationIllustration() {
    Canvas(modifier = Modifier.size(150.dp)) {
        drawRoundRect(
            color = LearnerPrimaryTint,
            topLeft = Offset(size.width * 0.32f, size.height * 0.18f),
            size = Size(size.width * 0.42f, size.height * 0.48f),
            cornerRadius = CornerRadius(18f, 18f)
        )
        drawCircle(
            color = LearnerPrimary.copy(alpha = 0.18f),
            radius = size.minDimension * 0.18f,
            center = Offset(size.width * 0.50f, size.height * 0.62f)
        )
        drawCircle(
            color = LearnerText,
            radius = size.minDimension * 0.06f,
            center = Offset(size.width * 0.50f, size.height * 0.40f)
        )
        drawRoundRect(
            color = LearnerPrimary,
            topLeft = Offset(size.width * 0.38f, size.height * 0.46f),
            size = Size(size.width * 0.24f, size.height * 0.22f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawLine(
            color = LearnerCoral.copy(alpha = 0.55f),
            start = Offset(size.width * 0.36f, size.height * 0.54f),
            end = Offset(size.width * 0.24f, size.height * 0.50f),
            strokeWidth = 6f
        )
        drawLine(
            color = LearnerCoral.copy(alpha = 0.55f),
            start = Offset(size.width * 0.64f, size.height * 0.54f),
            end = Offset(size.width * 0.76f, size.height * 0.48f),
            strokeWidth = 6f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(size.width * 0.43f, size.height * 0.28f),
            end = Offset(size.width * 0.66f, size.height * 0.28f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(size.width * 0.43f, size.height * 0.38f),
            end = Offset(size.width * 0.64f, size.height * 0.38f),
            strokeWidth = 4f
        )
    }
}

@Composable
fun NotificationItemCard(title: String, subtitle: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                NavGlyph(index = 2, color = accent)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "agora",
                color = LearnerMutedText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


