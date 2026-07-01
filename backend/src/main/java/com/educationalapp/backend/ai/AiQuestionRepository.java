package com.educationalapp.backend.ai;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiQuestionRepository extends JpaRepository<AiQuestion, Long> {
    List<AiQuestion> findTop100ByQuestionContainingIgnoreCaseOrSubjectContainingIgnoreCaseOrderByIdDesc(String question, String subject);
    List<AiQuestion> findTop100ByOrderByIdDesc();
}
