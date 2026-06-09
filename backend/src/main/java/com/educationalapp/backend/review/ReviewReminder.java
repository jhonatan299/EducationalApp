package com.educationalapp.backend.review;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class ReviewReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long studySessionId;
    private String title;
    private String subject;
    private LocalDate dueDate;
    private boolean completed;
    private LocalDateTime createdAt;

    protected ReviewReminder() {
    }

    public ReviewReminder(Long userId, Long studySessionId, String title, String subject, LocalDate dueDate) {
        this.userId = userId;
        this.studySessionId = studySessionId;
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getStudySessionId() {
        return studySessionId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void complete() {
        this.completed = true;
    }
}
