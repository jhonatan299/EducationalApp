package com.educationalapp.backend.ai;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiQuestionListRepository extends JpaRepository<AiQuestionList, Long> {
    List<AiQuestionList> findTop100ByOrderByCreatedAtDesc();
}
