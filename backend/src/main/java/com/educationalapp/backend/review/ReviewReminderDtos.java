package com.educationalapp.backend.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public final class ReviewReminderDtos {
    private ReviewReminderDtos() {
    }

    public record ReviewReminderResponse(
            Long id,
            Long userId,
            Long studySessionId,
            String title,
            String subject,
            LocalDate dueDate,
            boolean completed
    ) {
        public static ReviewReminderResponse from(ReviewReminder reminder) {
            return new ReviewReminderResponse(
                    reminder.getId(),
                    reminder.getUserId(),
                    reminder.getStudySessionId(),
                    reminder.getTitle(),
                    reminder.getSubject(),
                    reminder.getDueDate(),
                    reminder.isCompleted()
            );
        }
    }

    public record CreateReviewReminderRequest(
            @NotNull Long userId,
            @NotBlank String title,
            @NotBlank String subject,
            @NotNull LocalDate dueDate
    ) {
    }
}
