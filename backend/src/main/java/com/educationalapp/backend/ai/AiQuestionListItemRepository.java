package com.educationalapp.backend.ai;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiQuestionListItemRepository extends JpaRepository<AiQuestionListItem, Long> {
    List<AiQuestionListItem> findByListId(Long listId);
    boolean existsByListIdAndQuestionId(Long listId, Long questionId);
    void deleteByListId(Long listId);
    void deleteByQuestionId(Long questionId);
    void deleteByListIdAndQuestionId(Long listId, Long questionId);
    long countByListId(Long listId);
}
