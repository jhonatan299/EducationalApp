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
    private String topic;
    private String taskTitle;
    private String notes;
    private String originalDifficulty;
    private LocalDate dueDate;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    protected ReviewReminder() {
    }

    public ReviewReminder(Long userId, Long studySessionId, String title, String subject, LocalDate dueDate) {
        this(userId, studySessionId, title, subject, null, null, null, null, dueDate);
    }

    public ReviewReminder(Long userId, Long studySessionId, String title, String subject, String topic,
                          String taskTitle, String notes, String originalDifficulty, LocalDate dueDate) {
        this.userId = userId;
        this.studySessionId = studySessionId;
        this.title = title;
        this.subject = subject;
        this.topic = topic;
        this.taskTitle = taskTitle;
        this.notes = notes;
        this.originalDifficulty = originalDifficulty;
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

    public String getTopic() {
        return topic;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getNotes() {
        return notes;
    }

    public String getOriginalDifficulty() {
        return originalDifficulty;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void complete() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
    }
}
