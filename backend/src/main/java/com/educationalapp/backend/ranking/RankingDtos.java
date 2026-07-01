package com.educationalapp.backend.ranking;

import java.util.List;

public final class RankingDtos {
    private RankingDtos() {
    }

    public record RankingPlayerResponse(
            long userId,
            String name,
            int minutes,
            int position,
            boolean currentUser
    ) {
    }

    public record RankingResponse(
            String period,
            List<RankingPlayerResponse> players
    ) {
    }
}
