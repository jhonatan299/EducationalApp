package com.educationalapp.backend.calendar;

import java.time.LocalDate;
import java.util.List;

public final class CalendarDtos {
    private CalendarDtos() {
    }

    public record SubjectMinutes(String subject, int minutes) {
    }

    public record CalendarDayResponse(
            LocalDate date,
            int totalMinutes,
            List<SubjectMinutes> subjects
    ) {
    }
}
