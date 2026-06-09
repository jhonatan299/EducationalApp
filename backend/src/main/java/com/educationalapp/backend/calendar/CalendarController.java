package com.educationalapp.backend.calendar;

import com.educationalapp.backend.calendar.CalendarDtos.CalendarDayResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/user/{userId}")
    public List<CalendarDayResponse> month(@PathVariable Long userId,
                                           @RequestParam(required = false) Integer year,
                                           @RequestParam(required = false) Integer month) {
        LocalDate today = LocalDate.now();
        return calendarService.month(
                userId,
                year == null ? today.getYear() : year,
                month == null ? today.getMonthValue() : month
        );
    }
}
