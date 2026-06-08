package com.educationalapp.backend.stats;

import com.educationalapp.backend.stats.StatsDtos.DailyStudyMinutes;
import com.educationalapp.backend.stats.StatsDtos.ProgressResponse;
import com.educationalapp.backend.stats.StatsDtos.SubjectStudyMinutes;
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
public class StatsService {
    private final StudySessionRepository studySessionRepository;

    public StatsService(StudySessionRepository studySessionRepository) {
        this.studySessionRepository = studySessionRepository;
    }

    public ProgressResponse progress(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(days - 1L).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        List<StudySession> sessions = studySessionRepository.findByUserIdAndStartedAtBetween(userId, from, to);

        int totalMinutes = sessions.stream().mapToInt(StudySession::getDurationMinutes).sum();

        Map<LocalDate, Integer> byDayMap = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStartedAt().toLocalDate(),
                        Collectors.summingInt(StudySession::getDurationMinutes)
                ));
        List<DailyStudyMinutes> byDay = byDayMap.entrySet().stream()
                .map(entry -> new DailyStudyMinutes(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyStudyMinutes::date))
                .toList();

        Map<String, Integer> bySubjectMap = sessions.stream()
                .collect(Collectors.groupingBy(
                        StudySession::getSubject,
                        Collectors.summingInt(StudySession::getDurationMinutes)
                ));
        List<SubjectStudyMinutes> bySubject = bySubjectMap.entrySet().stream()
                .map(entry -> new SubjectStudyMinutes(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(SubjectStudyMinutes::minutes).reversed())
                .toList();

        return new ProgressResponse(userId, totalMinutes, byDay, bySubject);
    }
}
