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
fun CalendarScreen(calendar: List<CalendarDayResponse>) {
    val totalMinutes = calendar.sumOf { it.totalMinutes }
    val studiedDays = calendar.size
    val calendarByDate = calendar.associateBy { it.date }
    var selectedMode by remember { mutableIntStateOf(0) }
    var selectedDate by remember(calendar) { mutableStateOf(calendar.firstOrNull()?.date ?: todayIsoDate()) }
    var dateInput by remember { mutableStateOf(selectedDate) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CalendarTopBar(
            subtitle = "$studiedDays dias com estudo • $totalMinutes min"
        )

        OutlinedTextField(
            value = dateInput,
            onValueChange = { dateInput = it },
            label = { Text("Ir para a data (AAAA-MM-DD)") },
            singleLine = true,
            leadingIcon = { SearchGlyph(color = LearnerPrimary) },
            trailingIcon = {
                TextButton(
                    onClick = {
                        if (dateInput.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            selectedDate = formatIsoDate(parseIsoDate(dateInput))
                            dateInput = selectedDate
                        }
                    }
                ) {
                    Text("Ir", color = LearnerPrimary, fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        CalendarModeSelector(
            selectedMode = selectedMode,
            onModeSelected = { selectedMode = it }
        )

        when (selectedMode) {
            0 -> {
                MonthCalendarView(
                    selectedDate = selectedDate,
                    calendarByDate = calendarByDate,
                    onDateSelected = {
                        selectedDate = it
                        dateInput = it
                    },
                    onMonthChange = {
                        selectedDate = moveMonth(selectedDate, it)
                        dateInput = selectedDate
                    }
                )
                SelectedDayStudyCard(calendarByDate[selectedDate])
            }
            1 -> {
                WeekCalendarView(
                    selectedDate = selectedDate,
                    calendarByDate = calendarByDate,
                    onDateSelected = { selectedDate = it }
                )
            }
            else -> AgendaCalendarView(calendar)
        }
    }
}

@Composable
fun CalendarTopBar(subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Calendário",
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
}

@Composable
fun CalendarActionButton(
    background: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun MonthArrowGlyph(direction: Int, color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val startX = if (direction < 0) size.width * 0.62f else size.width * 0.38f
        val endX = if (direction < 0) size.width * 0.38f else size.width * 0.62f
        drawLine(color, Offset(startX, size.height * 0.2f), Offset(endX, size.height * 0.5f), strokeWidth = 3f)
        drawLine(color, Offset(endX, size.height * 0.5f), Offset(startX, size.height * 0.8f), strokeWidth = 3f)
    }
}

@Composable
fun CalendarModeSelector(selectedMode: Int, onModeSelected: (Int) -> Unit) {
    val modes = listOf("Mês", "Semana", "Agenda")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        modes.forEachIndexed { index, mode ->
            val selected = selectedMode == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) LearnerPrimary else Color.Transparent)
                    .clickable { onModeSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MonthCalendarView(
    selectedDate: String,
    calendarByDate: Map<String, CalendarDayResponse>,
    onDateSelected: (String) -> Unit,
    onMonthChange: (Int) -> Unit
) {
    val days = remember(selectedDate) { buildMonthDays(selectedDate) }
    val weeks = days.chunked(7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarActionButton(onClick = { onMonthChange(-1) }) {
                MonthArrowGlyph(direction = -1, color = LearnerPrimary)
            }
            Text(monthTitlePt(selectedDate), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            CalendarActionButton(onClick = { onMonthChange(1) }) {
                MonthArrowGlyph(direction = 1, color = LearnerPrimary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    MonthDayCell(
                        day = day,
                        selected = day.date == selectedDate,
                        studyDay = calendarByDate[day.date],
                        onClick = { onDateSelected(day.date) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
}


@Composable
fun MonthDayCell(
    day: CalendarDayCell,
    selected: Boolean,
    studyDay: CalendarDayResponse?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> LearnerPrimary
        studyDay != null -> LearnerPrimaryTint.copy(alpha = 0.75f)
        else -> Color.Transparent
    }
    val textColor = when {
        selected -> Color.White
        day.inCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }

    Box(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(5.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = day.dayOfMonth.toString().padStart(2, '0'),
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            if (studyDay != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (selected) Color.White else LearnerTeal)
                )
                Text(
                    text = "${studyDay.totalMinutes}m",
                    color = if (selected) Color.White else LearnerPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SelectedDayStudyCard(day: CalendarDayResponse?) {
    if (day == null) {
        EmptyStateCard(
            title = "Sem estudo neste dia",
            subtitle = "Salve uma sessão para ela aparecer no calendário."
        )
        return
    }

    CalendarDayCard(day)
}

@Composable
fun WeekCalendarView(
    selectedDate: String,
    calendarByDate: Map<String, CalendarDayResponse>,
    onDateSelected: (String) -> Unit
) {
    val weekDates = remember(selectedDate) { buildWeekDates(selectedDate) }

    SectionCard("Semana de ${formatShortDate(weekDates.first())}") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            weekDates.forEach { date ->
                val selected = date == selectedDate
                val studyDay = calendarByDate[date]
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) LearnerPrimary else MaterialTheme.colorScheme.background)
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = weekdayTiny(date),
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = date.takeLast(2),
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (studyDay != null) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color.White else LearnerTeal)
                            )
                        }
                    }
                }
            }
        }
    }

    val weekStudyDays = weekDates.mapNotNull { calendarByDate[it] }
    if (weekStudyDays.isEmpty()) {
        EmptyStateCard(
            title = "Semana livre",
            subtitle = "Nenhuma sessão registrada nessa semana."
        )
    } else {
        weekStudyDays.forEach { day ->
            CalendarStudyEventCard(day)
        }
    }
}

@Composable
fun AgendaCalendarView(calendar: List<CalendarDayResponse>) {
    val nextDays = calendar.sortedBy { it.date }

    if (nextDays.isEmpty()) {
        EmptyStateCard(
            title = "Sem eventos próximos",
            subtitle = "Suas sessões aparecerão aqui quando forem criadas."
        )
        return
    }

    SectionHeader(
        title = "Agenda",
        subtitle = "${nextDays.size} dias com atividade"
    )

    nextDays.forEach { day ->
        CalendarStudyEventCard(day)
    }
}

@Composable
fun CalendarStudyEventCard(day: CalendarDayResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LearnerPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 0.86f, size.height * 0.12f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.18f,
                    center = Offset(size.width * 0.55f, size.height * 0.92f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.22f),
                    topLeft = Offset(size.width * 0.72f, size.height * 0.25f),
                    size = Size(size.width * 0.18f, size.height * 0.30f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 4f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatReadableDate(day.date),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(40.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${day.totalMinutes} min",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                day.subjects.forEach { subject ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = subject.subject,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${subject.minutes} min",
                            color = Color.White.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

data class CalendarDayCell(
    val date: String,
    val dayOfMonth: Int,
    val inCurrentMonth: Boolean
)

fun buildMonthDays(selectedDate: String): List<CalendarDayCell> {
    val selected = parseIsoDate(selectedDate)
    selected.set(Calendar.DAY_OF_MONTH, 1)
    val month = selected.get(Calendar.MONTH)
    val firstDayOffset = (selected.get(Calendar.DAY_OF_WEEK) + 5) % 7
    selected.add(Calendar.DAY_OF_MONTH, -firstDayOffset)

    return (0 until 42).map {
        val item = selected.clone() as Calendar
        selected.add(Calendar.DAY_OF_MONTH, 1)
        CalendarDayCell(
            date = formatIsoDate(item),
            dayOfMonth = item.get(Calendar.DAY_OF_MONTH),
            inCurrentMonth = item.get(Calendar.MONTH) == month
        )
    }
}

fun buildWeekDates(selectedDate: String): List<String> {
    val selected = parseIsoDate(selectedDate)
    val mondayOffset = (selected.get(Calendar.DAY_OF_WEEK) + 5) % 7
    selected.add(Calendar.DAY_OF_MONTH, -mondayOffset)

    return (0 until 7).map {
        val item = selected.clone() as Calendar
        selected.add(Calendar.DAY_OF_MONTH, 1)
        formatIsoDate(item)
    }
}

fun moveMonth(date: String, delta: Int): String {
    return formatIsoDate(parseIsoDate(date).apply { add(Calendar.MONTH, delta) })
}

fun parseIsoDate(value: String): Calendar {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val parsed = runCatching { formatter.parse(value) }.getOrNull()
    return Calendar.getInstance().apply {
        if (parsed != null) {
            time = parsed
        }
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

fun formatIsoDate(calendar: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}

fun todayIsoDate(): String {
    return formatIsoDate(Calendar.getInstance())
}

fun monthTitlePt(date: String): String {
    return SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("pt-BR"))
        .format(parseIsoDate(date).time)
        .replaceFirstChar { it.uppercaseChar() }
}

fun formatShortDate(date: String): String {
    return SimpleDateFormat("dd/MM", Locale.forLanguageTag("pt-BR")).format(parseIsoDate(date).time)
}

fun formatReadableDate(date: String): String {
    return SimpleDateFormat("EEE, dd MMM", Locale.forLanguageTag("pt-BR"))
        .format(parseIsoDate(date).time)
        .replaceFirstChar { it.uppercaseChar() }
}

fun weekdayTiny(date: String): String {
    return SimpleDateFormat("EEE", Locale.forLanguageTag("pt-BR"))
        .format(parseIsoDate(date).time)
        .take(3)
        .replaceFirstChar { it.uppercaseChar() }
}

