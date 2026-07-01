package com.educationalapp.backend.review;

import com.educationalapp.backend.review.ReviewReminderDtos.CreateReviewReminderRequest;
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

    public ReviewReminder create(CreateReviewReminderRequest request) {
        return reviewReminderRepository.save(new ReviewReminder(
                request.userId(),
                null,
                request.title().trim(),
                request.subject().trim(),
                request.dueDate()
        ));
    }

    @Transactional
    public ReviewReminder complete(Long reminderId) {
        ReviewReminder reminder = reviewReminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Lembrete de revisao nao encontrado: " + reminderId));
        reminder.complete();
        return reminder;
    }

    public void delete(Long reminderId) {
        if (!reviewReminderRepository.existsById(reminderId)) {
            throw new IllegalArgumentException("Lembrete de revisao nao encontrado: " + reminderId);
        }
        reviewReminderRepository.deleteById(reminderId);
    }
}
