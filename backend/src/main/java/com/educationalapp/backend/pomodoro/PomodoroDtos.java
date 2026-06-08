package com.educationalapp.backend.pomodoro;

import com.educationalapp.backend.common.PomodoroStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class PomodoroDtos {
    private PomodoroDtos() {
    }

    public record StartPomodoroRequest(
            @NotNull Long userId,
            @NotBlank String subject,
            @Min(1) int focusMinutes,
            @Min(1) int breakMinutes
    ) {
    }

    public record CompletePomodoroRequest(String notes) {
    }

    public record PomodoroResponse(
            Long id,
            Long userId,
            String subject,
            int focusMinutes,
            int breakMinutes,
            int extendedMinutes,
            int totalFocusMinutes,
            PomodoroStatus status,
            LocalDateTime startedAt,
            LocalDateTime pausedAt,
            LocalDateTime completedAt,
            String notes
    ) {
        public static PomodoroResponse from(PomodoroSession session) {
            return new PomodoroResponse(
                    session.getId(),
                    session.getUserId(),
                    session.getSubject(),
                    session.getFocusMinutes(),
                    session.getBreakMinutes(),
                    session.getExtendedMinutes(),
                    session.totalFocusMinutes(),
                    session.getStatus(),
                    session.getStartedAt(),
                    session.getPausedAt(),
                    session.getCompletedAt(),
                    session.getNotes()
            );
        }
    }
}
