package com.educationalapp.backend.ai;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AiQuestionListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long listId;
    private Long questionId;

    protected AiQuestionListItem() {
    }

    public AiQuestionListItem(Long listId, Long questionId) {
        this.listId = listId;
        this.questionId = questionId;
    }

    public Long getId() {
        return id;
    }

    public Long getListId() {
        return listId;
    }

    public Long getQuestionId() {
        return questionId;
    }
}
