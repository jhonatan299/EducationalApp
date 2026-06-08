package com.educationalapp.backend.stats;

import java.time.LocalDate;
import java.util.List;

public final class StatsDtos {
    private StatsDtos() {
    }

    public record DailyStudyMinutes(LocalDate date, int minutes) {
    }

    public record SubjectStudyMinutes(String subject, int minutes) {
    }

    public record ProgressResponse(
            Long userId,
            int totalMinutes,
            List<DailyStudyMinutes> byDay,
            List<SubjectStudyMinutes> bySubject
    ) {
    }
}
