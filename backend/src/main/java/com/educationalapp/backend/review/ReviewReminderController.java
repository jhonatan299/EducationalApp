package com.educationalapp.backend.review;

import com.educationalapp.backend.review.ReviewReminderDtos.ReviewReminderResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewReminderController {
    private final ReviewReminderService reviewReminderService;

    public ReviewReminderController(ReviewReminderService reviewReminderService) {
        this.reviewReminderService = reviewReminderService;
    }

    @GetMapping("/user/{userId}")
    public List<ReviewReminderResponse> listForUser(@PathVariable Long userId) {
        return reviewReminderService.listForUser(userId).stream()
                .map(ReviewReminderResponse::from)
                .toList();
    }

    @PostMapping("/{reminderId}/complete")
    public ReviewReminderResponse complete(@PathVariable Long reminderId) {
        return ReviewReminderResponse.from(reviewReminderService.complete(reminderId));
    }
}
