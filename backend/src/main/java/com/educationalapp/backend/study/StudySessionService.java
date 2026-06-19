package com.educationalapp.backend.study;

import com.educationalapp.backend.common.SessionSource;
import com.educationalapp.backend.common.StudyType;
import com.educationalapp.backend.goal.Goal;
import com.educationalapp.backend.goal.GoalRepository;
import com.educationalapp.backend.mission.Mission;
import com.educationalapp.backend.mission.MissionRepository;
import com.educationalapp.backend.review.ReviewReminder;
import com.educationalapp.backend.review.ReviewReminderRepository;
import com.educationalapp.backend.study.StudySessionDtos.ManualStudySessionRequest;
import com.educationalapp.backend.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudySessionService {
    private final StudySessionRepository studySessionRepository;
    private final GoalRepository goalRepository;
    private final MissionRepository missionRepository;
    private final ReviewReminderRepository reviewReminderRepository;
    private final UserService userService;

    public StudySessionService(StudySessionRepository studySessionRepository, GoalRepository goalRepository,
                               MissionRepository missionRepository, ReviewReminderRepository reviewReminderRepository,
                               UserService userService) {
        this.studySessionRepository = studySessionRepository;
        this.goalRepository = goalRepository;
        this.missionRepository = missionRepository;
        this.reviewReminderRepository = reviewReminderRepository;
        this.userService = userService;
    }

    @Transactional
    public StudySession createManualSession(ManualStudySessionRequest request) {
        LocalDateTime endedAt = LocalDateTime.now();
        LocalDateTime startedAt = endedAt.minusMinutes(request.durationMinutes());
        return createSession(request.userId(), request.subject(), request.durationMinutes(), SessionSource.MANUAL,
                startedAt, endedAt, request.studyType(), request.content(), request.objective(), request.notes(),
                request.learned(), request.difficulties(), request.nextAction(), request.focusRating());
    }

    @Transactional
    public StudySession createPomodoroSession(Long userId, String subject, int minutes,
                                              LocalDateTime startedAt, LocalDateTime endedAt, String notes) {
        return createSession(userId, subject, minutes, SessionSource.POMODORO, startedAt, endedAt,
                StudyType.REVIEW, null, "Concluir um Pomodoro com foco", notes, null, null, null, null);
    }

    public List<StudySession> listForUser(Long userId) {
        return studySessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    private StudySession createSession(Long userId, String subject, int minutes, SessionSource source,
                                       LocalDateTime startedAt, LocalDateTime endedAt, StudyType studyType,
                                       String content, String objective, String notes, String learned,
                                       String difficulties, String nextAction, Integer focusRating) {
        userService.getUser(userId);
        StudySession session = studySessionRepository.save(
                new StudySession(userId, subject, minutes, source, startedAt, endedAt, content, objective,
                        notes, learned, difficulties, nextAction, focusRating, studyType)
        );
        applyProgress(userId, subject, minutes);
        createReviewReminder(session);
        userService.addCoins(userId, Math.max(1, minutes / 5), "Sessão de estudo: " + subject);
        return session;
    }

    private void createReviewReminder(StudySession session) {
        String title = session.getContent() == null || session.getContent().isBlank()
                ? "Revisar " + session.getSubject()
                : "Revisar " + session.getContent();
        reviewReminderRepository.save(new ReviewReminder(
                session.getUserId(),
                session.getId(),
                title,
                session.getSubject(),
                session.getEndedAt().toLocalDate().plusDays(1)
        ));
    }

    private void applyProgress(Long userId, String subject, int minutes) {
        List<Goal> goals = goalRepository.findByUserIdOrderByDueDateAsc(userId);
        goals.stream()
                .filter(goal -> goal.getSubject().equalsIgnoreCase(subject))
                .forEach(goal -> goal.addProgress(minutes));

        List<Mission> missions = missionRepository.findByUserIdOrderByIdAsc(userId);
        missions.stream()
                .filter(mission -> !mission.isCompleted())
                .forEach(mission -> mission.addProgress(minutes));
    }
}
