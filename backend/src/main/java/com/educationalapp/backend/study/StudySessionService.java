package com.educationalapp.backend.study;

import com.educationalapp.backend.common.SessionSource;
import com.educationalapp.backend.goal.Goal;
import com.educationalapp.backend.goal.GoalRepository;
import com.educationalapp.backend.mission.Mission;
import com.educationalapp.backend.mission.MissionRepository;
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
    private final UserService userService;

    public StudySessionService(StudySessionRepository studySessionRepository, GoalRepository goalRepository,
                               MissionRepository missionRepository, UserService userService) {
        this.studySessionRepository = studySessionRepository;
        this.goalRepository = goalRepository;
        this.missionRepository = missionRepository;
        this.userService = userService;
    }

    @Transactional
    public StudySession createManualSession(ManualStudySessionRequest request) {
        LocalDateTime endedAt = LocalDateTime.now();
        LocalDateTime startedAt = endedAt.minusMinutes(request.durationMinutes());
        return createSession(request.userId(), request.subject(), request.durationMinutes(), SessionSource.MANUAL,
                startedAt, endedAt, request.notes());
    }

    @Transactional
    public StudySession createPomodoroSession(Long userId, String subject, int minutes,
                                              LocalDateTime startedAt, LocalDateTime endedAt, String notes) {
        return createSession(userId, subject, minutes, SessionSource.POMODORO, startedAt, endedAt, notes);
    }

    public List<StudySession> listForUser(Long userId) {
        return studySessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    private StudySession createSession(Long userId, String subject, int minutes, SessionSource source,
                                       LocalDateTime startedAt, LocalDateTime endedAt, String notes) {
        userService.getUser(userId);
        StudySession session = studySessionRepository.save(
                new StudySession(userId, subject, minutes, source, startedAt, endedAt, notes)
        );
        applyProgress(userId, subject, minutes);
        userService.addCoins(userId, Math.max(1, minutes / 5), "Study session: " + subject);
        return session;
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
