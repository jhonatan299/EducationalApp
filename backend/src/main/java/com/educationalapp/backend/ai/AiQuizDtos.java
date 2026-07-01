package com.educationalapp.backend.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class AiQuizDtos {
    public record GenerateQuizRequest(
            @NotBlank @Size(max = 9000) String text,
            String subject,
            String gameMode,
            Integer questionCount
    ) {
    }

    public record GeneratedQuizResponse(
            String source,
            String gameMode,
            List<GeneratedQuestion> questions
    ) {
    }

    public record GeneratedQuestion(
            String question,
            List<String> options,
            int correctIndex,
            String explanation
    ) {
    }

    public record SavedQuestionResponse(
            Long id,
            String subject,
            String gameMode,
            String question,
            List<String> options,
            int correctIndex,
            String explanation
    ) {
        public static SavedQuestionResponse from(AiQuestion question) {
            return new SavedQuestionResponse(
                    question.getId(),
                    question.getSubject(),
                    question.getGameMode(),
                    question.getQuestion(),
                    List.of(question.getOptionA(), question.getOptionB(), question.getOptionC(), question.getOptionD()),
                    question.getCorrectIndex(),
                    question.getExplanation()
            );
        }
    }

    public record UpdateQuestionRequest(
            String subject,
            String gameMode
    ) {
    }

    public record SaveQuestionsRequest(
            String subject,
            String gameMode,
            List<GeneratedQuestion> questions
    ) {
    }

    public record QuestionListRequest(
            @NotBlank String name
    ) {
    }

    public record QuestionListResponse(
            Long id,
            String name,
            long questionCount,
            List<Long> questionIds
    ) {
    }

    public record GeminiQuizPayload(
            List<GeneratedQuestion> questions
    ) {
    }
}
