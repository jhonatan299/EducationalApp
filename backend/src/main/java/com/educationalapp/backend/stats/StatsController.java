package com.educationalapp.backend.stats;

import com.educationalapp.backend.stats.StatsDtos.ProgressResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/user/{userId}/progress")
    public ProgressResponse progress(@PathVariable Long userId,
                                     @RequestParam(defaultValue = "7") int days) {
        return statsService.progress(userId, days);
    }
}
