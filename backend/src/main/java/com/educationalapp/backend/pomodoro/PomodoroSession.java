package com.educationalapp.backend.pomodoro;

import com.educationalapp.backend.common.PomodoroStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class PomodoroSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String subject;
    private int focusMinutes;
    private int breakMinutes;
    private int extendedMinutes;

    @Enumerated(EnumType.STRING)
    private PomodoroStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime pausedAt;
    private LocalDateTime completedAt;
    private String notes;

    protected PomodoroSession() {
    }

    public PomodoroSession(Long userId, String subject, int focusMinutes, int breakMinutes) {
        this.userId = userId;
        this.subject = subject;
        this.focusMinutes = focusMinutes;
        this.breakMinutes = breakMinutes;
        this.extendedMinutes = 0;
        this.status = PomodoroStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSubject() {
        return subject;
    }

    public int getFocusMinutes() {
        return focusMinutes;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }

    public int getExtendedMinutes() {
        return extendedMinutes;
    }

    public PomodoroStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getPausedAt() {
        return pausedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getNotes() {
        return notes;
    }

    public int totalFocusMinutes() {
        return focusMinutes + extendedMinutes;
    }

    public void pause() {
        this.status = PomodoroStatus.PAUSED;
        this.pausedAt = LocalDateTime.now();
    }

    public void resume() {
        this.status = PomodoroStatus.RUNNING;
        this.pausedAt = null;
    }

    public void extendFiveMinutes() {
        this.extendedMinutes += 5;
    }

    public void complete(String notes) {
        this.status = PomodoroStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.notes = notes;
    }
}
