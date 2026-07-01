package com.educationalapp.backend.ranking;

import com.educationalapp.backend.ranking.RankingDtos.RankingPlayerResponse;
import com.educationalapp.backend.ranking.RankingDtos.RankingResponse;
import com.educationalapp.backend.study.StudySessionRepository;
import com.educationalapp.backend.user.AppUserRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RankingService {
    private final StudySessionRepository studySessionRepository;
    private final AppUserRepository appUserRepository;

    public RankingService(StudySessionRepository studySessionRepository, AppUserRepository appUserRepository) {
        this.studySessionRepository = studySessionRepository;
        this.appUserRepository = appUserRepository;
    }

    public RankingResponse weekly(Long currentUserId) {
        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
        return buildRanking("WEEKLY", studySessionRepository.rankingFrom(weekStart), currentUserId);
    }

    public RankingResponse allTime(Long currentUserId) {
        return buildRanking("ALL_TIME", studySessionRepository.rankingAllTime(), currentUserId);
    }

    private RankingResponse buildRanking(String period, List<Object[]> rows, Long currentUserId) {
        Map<Long, String> namesById = appUserRepository.findAll().stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user.getName()));

        List<RankingPlayerResponse> players = new ArrayList<>();
        for (int index = 0; index < rows.size() && index < 20; index++) {
            Object[] row = rows.get(index);
            Long userId = ((Number) row[0]).longValue();
            int minutes = ((Number) row[1]).intValue();
            players.add(new RankingPlayerResponse(
                    userId,
                    namesById.getOrDefault(userId, "Usuario"),
                    minutes,
                    index + 1,
                    currentUserId != null && currentUserId.equals(userId)
            ));
        }

        return new RankingResponse(period, players);
    }
}
