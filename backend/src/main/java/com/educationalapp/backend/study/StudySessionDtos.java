package com.educationalapp.backend.study;

import com.educationalapp.backend.common.SessionSource;
import com.educationalapp.backend.common.StudyType;
import jakarta.validation.constraints.Max;
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
            StudyType studyType,
            String content,
            String objective,
            String notes,
            String learned,
            String difficulties,
            String nextAction,
            @Min(1) @Max(5) Integer focusRating,
            @Min(0) Integer exerciseCount,
            @Min(0) Integer correctAnswers
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
            StudyType studyType,
            String content,
            String objective,
            String notes,
            String learned,
            String difficulties,
            String nextAction,
            Integer focusRating,
            Integer exerciseCount,
            Integer correctAnswers
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
                    session.getStudyType(),
                    session.getContent(),
                    session.getObjective(),
                    session.getNotes(),
                    session.getLearned(),
                    session.getDifficulties(),
                    session.getNextAction(),
                    session.getFocusRating(),
                    session.getExerciseCount(),
                    session.getCorrectAnswers()
            );
        }
    }
}
