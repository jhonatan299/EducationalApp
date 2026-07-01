package com.educationalapp.backend.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReminderRepository extends JpaRepository<ReviewReminder, Long> {
    List<ReviewReminder> findByUserIdOrderByDueDateAsc(Long userId);

    void deleteByStudySessionId(Long studySessionId);
}
