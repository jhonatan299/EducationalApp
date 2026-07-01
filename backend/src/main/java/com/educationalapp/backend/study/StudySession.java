package com.educationalapp.backend.study;

import com.educationalapp.backend.common.SessionSource;
import com.educationalapp.backend.common.StudyType;
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
    private String content;
    private String objective;
    private String notes;
    private String learned;
    private String difficulties;
    private String nextAction;
    private Integer focusRating;
    private Integer exerciseCount;
    private Integer correctAnswers;

    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    protected StudySession() {
    }

    public StudySession(Long userId, String subject, int durationMinutes, SessionSource source,
                        LocalDateTime startedAt, LocalDateTime endedAt, String content, String objective,
                        String notes, String learned, String difficulties, String nextAction,
                        Integer focusRating, StudyType studyType, Integer exerciseCount, Integer correctAnswers) {
        this.userId = userId;
        this.subject = subject;
        this.durationMinutes = durationMinutes;
        this.source = source;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.content = content;
        this.objective = objective;
        this.notes = notes;
        this.learned = learned;
        this.difficulties = difficulties;
        this.nextAction = nextAction;
        this.focusRating = focusRating;
        this.studyType = studyType;
        this.exerciseCount = exerciseCount;
        this.correctAnswers = correctAnswers;
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

    public String getContent() {
        return content;
    }

    public String getObjective() {
        return objective;
    }

    public String getLearned() {
        return learned;
    }

    public String getDifficulties() {
        return difficulties;
    }

    public String getNextAction() {
        return nextAction;
    }

    public Integer getFocusRating() {
        return focusRating;
    }

    public StudyType getStudyType() {
        return studyType;
    }

    public Integer getExerciseCount() {
        return exerciseCount;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }
}
