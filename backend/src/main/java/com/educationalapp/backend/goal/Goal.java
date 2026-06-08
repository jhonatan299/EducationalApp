package com.educationalapp.backend.goal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String subject;
    private int targetMinutes;
    private int currentMinutes;
    private LocalDate dueDate;
    private LocalDateTime createdAt;

    protected Goal() {
    }

    public Goal(Long userId, String title, String subject, int targetMinutes, LocalDate dueDate) {
        this.userId = userId;
        this.title = title;
        this.subject = subject;
        this.targetMinutes = targetMinutes;
        this.currentMinutes = 0;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public int getCurrentMinutes() {
        return currentMinutes;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCompleted() {
        return currentMinutes >= targetMinutes;
    }

    public long daysUntilDueDate() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    public void addProgress(int minutes) {
        this.currentMinutes += minutes;
    }
}
