package com.educationalapp.backend.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReviewReminderDtos {
    private ReviewReminderDtos() {
    }

    public record ReviewReminderResponse(
            Long id,
            Long userId,
            Long studySessionId,
            String title,
            String subject,
            String topic,
            String taskTitle,
            String notes,
            String originalDifficulty,
            LocalDate dueDate,
            boolean completed,
            LocalDateTime completedAt
    ) {
        public static ReviewReminderResponse from(ReviewReminder reminder) {
            return new ReviewReminderResponse(
                    reminder.getId(),
                    reminder.getUserId(),
                    reminder.getStudySessionId(),
                    reminder.getTitle(),
                    reminder.getSubject(),
                    reminder.getTopic(),
                    reminder.getTaskTitle(),
                    reminder.getNotes(),
                    reminder.getOriginalDifficulty(),
                    reminder.getDueDate(),
                    reminder.isCompleted(),
                    reminder.getCompletedAt()
            );
        }
    }

    public record CreateReviewReminderRequest(
            @NotNull Long userId,
            @NotBlank String title,
            @NotBlank String subject,
            String topic,
            String taskTitle,
            String notes,
            String originalDifficulty,
            @NotNull LocalDate dueDate
    ) {
    }
}
