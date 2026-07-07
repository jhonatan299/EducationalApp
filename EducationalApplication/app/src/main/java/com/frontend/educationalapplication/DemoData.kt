package com.frontend.educationalapplication

import com.frontend.educationalapplication.network.CalendarDayResponse
import com.frontend.educationalapplication.network.CalendarSubjectMinutes
import com.frontend.educationalapplication.network.DailyStudyMinutes
import com.frontend.educationalapplication.network.GoalResponse
import com.frontend.educationalapplication.network.MissionResponse
import com.frontend.educationalapplication.network.ProgressResponse
import com.frontend.educationalapplication.network.RankingPlayerResponse
import com.frontend.educationalapplication.network.RankingResponse
import com.frontend.educationalapplication.network.ReviewReminderResponse
import com.frontend.educationalapplication.network.StudySessionResponse
import com.frontend.educationalapplication.network.SubjectStudyMinutes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun demoScreenData(userId: Long): ScreenData {
    val today = demoDateAfter(0)
    val yesterday = demoDateAfter(-1)
    val tomorrow = demoDateAfter(1)
    val inThreeDays = demoDateAfter(3)

    val sessions = listOf(
        StudySessionResponse(
            id = 9001,
            subject = "Matemática",
            durationMinutes = 35,
            source = "MANUAL",
            startedAt = "${yesterday}T15:00:00",
            endedAt = "${yesterday}T15:35:00",
            studyType = "EXERCISES",
            content = "Funções do 1º grau",
            objective = "Resolver exercícios da lista da escola",
            notes = "Errei mais nas questões com gráfico.",
            learned = "Revisar coeficiente angular.",
            difficulties = "Interpretar o gráfico rápido.",
            nextAction = "Fazer mais 5 exercícios amanhã.",
            focusRating = 4,
            exerciseCount = 12,
            correctAnswers = 9
        ),
        StudySessionResponse(
            id = 9002,
            subject = "Biologia",
            durationMinutes = 25,
            source = "POMODORO",
            startedAt = "${today}T09:30:00",
            endedAt = "${today}T09:55:00",
            studyType = "READING",
            content = "Citologia",
            objective = "Revisar organelas celulares",
            notes = "Mitocôndria e ribossomos já estão ok.",
            learned = "Diferença entre retículo liso e rugoso.",
            difficulties = "",
            nextAction = "Fazer um resumo curto no caderno.",
            focusRating = 5,
            exerciseCount = null,
            correctAnswers = null
        )
    )

    val goals = listOf(
        GoalResponse(9101, "Revisar Matemática", "Matemática", 120, 75, 62.5, demoDateAfter(5), 5, false),
        GoalResponse(9102, "Estudar Biologia", "Biologia", 90, 55, 61.0, demoDateAfter(4), 4, false),
        GoalResponse(9103, "Preparar redação", "Português", 60, 20, 33.0, demoDateAfter(7), 7, false)
    )

    val missions = listOf(
        MissionResponse(9201, "Primeira sessão do dia", "Complete uma sessão de foco hoje.", 25, 25, 15, true, false),
        MissionResponse(9202, "Semana constante", "Estude 3 dias nesta semana.", 180, 95, 40, false, false)
    )

    val reviews = listOf(
        ReviewReminderResponse(
            id = 9301,
            title = "Revisar Funções",
            subject = "Matemática",
            topic = "Funções do 1º grau",
            taskTitle = "Refazer questões erradas",
            notes = "Olhar os gráficos da lista.",
            originalDifficulty = "OK",
            dueDate = tomorrow,
            completed = false,
            completedAt = null
        ),
        ReviewReminderResponse(
            id = 9302,
            title = "Revisar Citologia",
            subject = "Biologia",
            topic = "Organelas celulares",
            taskTitle = "Resumo rápido",
            notes = "Focar retículo e complexo de Golgi.",
            originalDifficulty = "EASY",
            dueDate = inThreeDays,
            completed = false,
            completedAt = null
        )
    )

    val calendar = listOf(
        CalendarDayResponse(yesterday, 35, listOf(CalendarSubjectMinutes("Matemática", 35))),
        CalendarDayResponse(today, 25, listOf(CalendarSubjectMinutes("Biologia", 25))),
        CalendarDayResponse(tomorrow, 0, listOf(CalendarSubjectMinutes("Revisão de Matemática", 0)))
    )

    val progress = ProgressResponse(
        userId = userId,
        totalMinutes = 95,
        byDay = listOf(
            DailyStudyMinutes(demoDateAfter(-6), 0),
            DailyStudyMinutes(demoDateAfter(-5), 15),
            DailyStudyMinutes(demoDateAfter(-4), 0),
            DailyStudyMinutes(demoDateAfter(-3), 20),
            DailyStudyMinutes(demoDateAfter(-2), 0),
            DailyStudyMinutes(yesterday, 35),
            DailyStudyMinutes(today, 25)
        ),
        bySubject = listOf(
            SubjectStudyMinutes("Matemática", 50),
            SubjectStudyMinutes("Biologia", 25),
            SubjectStudyMinutes("Português", 20)
        )
    )

    return ScreenData(sessions, goals, missions, reviews, calendar, progress)
}

fun demoStudyPlanningSubjects(): List<StudySubjectPlan> = listOf(
    StudySubjectPlan(
        id = 8001,
        name = "Matemática",
        priority = SubjectPriority.HIGH,
        difficulty = StudyDifficulty.HARD,
        examDate = demoDateAfter(14),
        estimatedHours = "6",
        weeklyGoalMinutes = "120",
        createdAt = System.currentTimeMillis(),
        topics = listOf(
            StudyTopicPlan(
                id = 8101,
                subjectId = 8001,
                name = "Funções do 1º grau",
                status = TopicStatus.IN_PROGRESS,
                difficulty = StudyDifficulty.MEDIUM,
                estimatedMinutes = "90",
                note = "Revisar gráficos e exercícios.",
                createdAt = System.currentTimeMillis(),
                tasks = listOf(
                    StudyTaskPlan(8201, "Ler resumo da aula", "Leitura", StudyTaskStatus.PENDING, 0),
                    StudyTaskPlan(8202, "Fazer 10 exercícios", "Exercício", StudyTaskStatus.PENDING, 1),
                    StudyTaskPlan(8203, "Revisar erros", "Revisão", StudyTaskStatus.PENDING, 2)
                )
            )
        )
    ),
    StudySubjectPlan(
        id = 8002,
        name = "Biologia",
        priority = SubjectPriority.MEDIUM,
        difficulty = StudyDifficulty.MEDIUM,
        examDate = demoDateAfter(10),
        estimatedHours = "4",
        weeklyGoalMinutes = "90",
        createdAt = System.currentTimeMillis(),
        topics = listOf(
            StudyTopicPlan(
                id = 8102,
                subjectId = 8002,
                name = "Citologia",
                status = TopicStatus.STUDIED,
                difficulty = StudyDifficulty.EASY,
                estimatedMinutes = "60",
                note = "Organizar resumo de organelas.",
                createdAt = System.currentTimeMillis(),
                tasks = listOf(
                    StudyTaskPlan(8204, "Revisar organelas", "Revisão", StudyTaskStatus.COMPLETED, 0),
                    StudyTaskPlan(8205, "Fazer mapa mental", "Resumo", StudyTaskStatus.PENDING, 1)
                )
            )
        )
    ),
    StudySubjectPlan(
        id = 8003,
        name = "Português",
        priority = SubjectPriority.MEDIUM,
        difficulty = StudyDifficulty.MEDIUM,
        examDate = "",
        estimatedHours = "3",
        weeklyGoalMinutes = "60",
        createdAt = System.currentTimeMillis(),
        topics = listOf(
            StudyTopicPlan(
                id = 8103,
                subjectId = 8003,
                name = "Redação argumentativa",
                status = TopicStatus.NOT_STARTED,
                difficulty = StudyDifficulty.MEDIUM,
                estimatedMinutes = "80",
                note = "Treinar introdução e repertório.",
                createdAt = System.currentTimeMillis(),
                tasks = listOf(
                    StudyTaskPlan(8206, "Separar repertórios", "Resumo", StudyTaskStatus.PENDING, 0),
                    StudyTaskPlan(8207, "Escrever uma introdução", "Exercício", StudyTaskStatus.PENDING, 1)
                )
            )
        )
    )
)

fun demoRanking(period: String, currentUserId: Long): RankingResponse {
    val allTime = period.equals("ALL_TIME", ignoreCase = true)
    val basePlayers = listOf(
        Triple(1001L, "João", if (allTime) 420 else 95),
        Triple(1002L, "Marina", if (allTime) 390 else 80),
        Triple(1003L, "Pedro", if (allTime) 310 else 70),
        Triple(1004L, "Ana", if (allTime) 260 else 55),
        Triple(1005L, "Rafael", if (allTime) 180 else 40)
    ).sortedByDescending { it.third }

    return RankingResponse(
        period = period,
        players = basePlayers.mapIndexed { index, player ->
            RankingPlayerResponse(
                userId = player.first,
                name = player.second,
                minutes = player.third,
                position = index + 1,
                currentUser = player.first == currentUserId || (currentUserId <= 0 && player.first == 1001L)
            )
        }
    )
}

private fun demoDateAfter(days: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
}
