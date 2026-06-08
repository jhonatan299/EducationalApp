package com.educationalapp.backend.study;

import com.educationalapp.backend.common.SessionSource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public final class StudySessionDtos {
    private StudySessionDtos() {
    }

    public record ManualStudySessionRequest(
            @NotNull Long userId,
            @NotBlank String subject,
            @Min(1) int durationMinutes,
            String notes
    ) {
    }

    public record StudySessionResponse(
            Long id,
            Long userId,
            String subject,
            int durationMinutes,
            SessionSource source,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            String notes
    ) {
        public static StudySessionResponse from(StudySession session) {
            return new StudySessionResponse(
                    session.getId(),
                    session.getUserId(),
                    session.getSubject(),
                    session.getDurationMinutes(),
                    session.getSource(),
                    session.getStartedAt(),
                    session.getEndedAt(),
                    session.getNotes()
            );
        }
    }
}
