package com.educationalapp.backend.ranking;

import com.educationalapp.backend.ranking.RankingDtos.RankingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {
    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/weekly")
    public RankingResponse weekly(@RequestParam(required = false) Long currentUserId) {
        return rankingService.weekly(currentUserId);
    }

    @GetMapping("/all-time")
    public RankingResponse allTime(@RequestParam(required = false) Long currentUserId) {
        return rankingService.allTime(currentUserId);
    }
}
