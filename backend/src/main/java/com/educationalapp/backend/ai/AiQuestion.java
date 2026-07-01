package com.educationalapp.backend.ai;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class AiQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;
    private String gameMode;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctIndex;
    private String explanation;
    private LocalDateTime createdAt;

    protected AiQuestion() {
    }

    public AiQuestion(String subject, String gameMode, String question, String optionA, String optionB,
                      String optionC, String optionD, int correctIndex, String explanation) {
        this.subject = subject;
        this.gameMode = gameMode;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getGameMode() { return gameMode; }
    public String getQuestion() { return question; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public int getCorrectIndex() { return correctIndex; }
    public String getExplanation() { return explanation; }

    public void reclassify(String subject, String gameMode) {
        if (subject != null && !subject.isBlank()) this.subject = subject.trim();
        if (gameMode != null && !gameMode.isBlank()) this.gameMode = gameMode.trim();
    }
}
