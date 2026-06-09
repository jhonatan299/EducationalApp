package com.educationalapp.backend.review;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewReminderService {
    private final ReviewReminderRepository reviewReminderRepository;

    public ReviewReminderService(ReviewReminderRepository reviewReminderRepository) {
        this.reviewReminderRepository = reviewReminderRepository;
    }

    public List<ReviewReminder> listForUser(Long userId) {
        return reviewReminderRepository.findByUserIdOrderByDueDateAsc(userId);
    }

    @Transactional
    public ReviewReminder complete(Long reminderId) {
        ReviewReminder reminder = reviewReminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Review reminder not found: " + reminderId));
        reminder.complete();
        return reminder;
    }
}
