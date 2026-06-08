package com.educationalapp.backend.goal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public final class GoalDtos {
    private GoalDtos() {
    }

    public record CreateGoalRequest(
            @NotNull Long userId,
            @NotBlank String title,
            @NotBlank String subject,
            @Min(1) int targetMinutes,
            @NotNull LocalDate dueDate
    ) {
    }

    public record GoalResponse(
            Long id,
            Long userId,
            String title,
            String subject,
            int targetMinutes,
            int currentMinutes,
            double progressPercent,
            LocalDate dueDate,
            long daysUntilDueDate,
            boolean completed
    ) {
        public static GoalResponse from(Goal goal) {
            double progress = Math.min(100.0, (goal.getCurrentMinutes() * 100.0) / goal.getTargetMinutes());
            return new GoalResponse(
                    goal.getId(),
                    goal.getUserId(),
                    goal.getTitle(),
                    goal.getSubject(),
                    goal.getTargetMinutes(),
                    goal.getCurrentMinutes(),
                    progress,
                    goal.getDueDate(),
                    goal.daysUntilDueDate(),
                    goal.isCompleted()
            );
        }
    }
}
