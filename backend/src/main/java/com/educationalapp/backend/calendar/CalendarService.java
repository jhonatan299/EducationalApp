package com.educationalapp.backend.calendar;

import com.educationalapp.backend.calendar.CalendarDtos.CalendarDayResponse;
import com.educationalapp.backend.calendar.CalendarDtos.SubjectMinutes;
import com.educationalapp.backend.study.StudySession;
import com.educationalapp.backend.study.StudySessionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {
    private final StudySessionRepository studySessionRepository;

    public CalendarService(StudySessionRepository studySessionRepository) {
        this.studySessionRepository = studySessionRepository;
    }

    public List<CalendarDayResponse> month(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to = startDate.plusMonths(1).atStartOfDay();
        List<StudySession> sessions = studySessionRepository.findByUserIdAndStartedAtBetween(userId, from, to);

        Map<LocalDate, List<StudySession>> byDate = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getStartedAt().toLocalDate()));

        return byDate.entrySet().stream()
                .map(entry -> {
                    int total = entry.getValue().stream().mapToInt(StudySession::getDurationMinutes).sum();
                    Map<String, Integer> subjectMap = entry.getValue().stream()
                            .collect(Collectors.groupingBy(
                                    StudySession::getSubject,
                                    Collectors.summingInt(StudySession::getDurationMinutes)
                            ));
                    List<SubjectMinutes> subjects = subjectMap.entrySet().stream()
                            .map(subject -> new SubjectMinutes(subject.getKey(), subject.getValue()))
                            .sorted(Comparator.comparing(SubjectMinutes::minutes).reversed())
                            .toList();
                    return new CalendarDayResponse(entry.getKey(), total, subjects);
                })
                .sorted(Comparator.comparing(CalendarDayResponse::date))
                .toList();
    }
}
