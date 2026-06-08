package com.educationalapp.backend.study;

import com.educationalapp.backend.common.SessionSource;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class StudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String subject;
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    private SessionSource source;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String notes;

    protected StudySession() {
    }

    public StudySession(Long userId, String subject, int durationMinutes, SessionSource source,
                        LocalDateTime startedAt, LocalDateTime endedAt, String notes) {
        this.userId = userId;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.source = source;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.notes = notes;
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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public SessionSource getSource() {
        return source;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public String getNotes() {
        return notes;
    }
}
