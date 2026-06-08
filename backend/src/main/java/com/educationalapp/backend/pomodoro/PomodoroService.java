package com.educationalapp.backend.pomodoro;

import com.educationalapp.backend.pomodoro.PomodoroDtos.StartPomodoroRequest;
import com.educationalapp.backend.study.StudySessionService;
import com.educationalapp.backend.user.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PomodoroService {
    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final StudySessionService studySessionService;
    private final UserService userService;

    public PomodoroService(PomodoroSessionRepository pomodoroSessionRepository,
                           StudySessionService studySessionService, UserService userService) {
        this.pomodoroSessionRepository = pomodoroSessionRepository;
        this.studySessionService = studySessionService;
        this.userService = userService;
    }

    @Transactional
    public PomodoroSession start(StartPomodoroRequest request) {
        userService.getUser(request.userId());
        return pomodoroSessionRepository.save(new PomodoroSession(
                request.userId(),
                request.subject(),
                request.focusMinutes(),
                request.breakMinutes()
        ));
    }

    @Transactional
    public PomodoroSession pause(Long id) {
        PomodoroSession session = get(id);
        session.pause();
        return session;
    }

    @Transactional
    public PomodoroSession resume(Long id) {
        PomodoroSession session = get(id);
        session.resume();
        return session;
    }

    @Transactional
    public PomodoroSession extendFiveMinutes(Long id) {
        PomodoroSession session = get(id);
        session.extendFiveMinutes();
        return session;
    }

    @Transactional
    public PomodoroSession complete(Long id, String notes) {
        PomodoroSession session = get(id);
        session.complete(notes);
        studySessionService.createPomodoroSession(
                session.getUserId(),
                session.getSubject(),
                session.totalFocusMinutes(),
                session.getStartedAt(),
                session.getCompletedAt(),
                notes
        );
        return session;
    }

    public List<PomodoroSession> listForUser(Long userId) {
        return pomodoroSessionRepository.findByUserIdOrderByStartedAtDesc(userId);
    }

    private PomodoroSession get(Long id) {
        return pomodoroSessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pomodoro not found: " + id));
    }
}
