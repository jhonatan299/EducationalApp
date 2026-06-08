package com.educationalapp.backend.pomodoro;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByUserIdOrderByStartedAtDesc(Long userId);
}
