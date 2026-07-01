package com.educationalapp.backend.study;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    List<StudySession> findByUserIdOrderByStartedAtDesc(Long userId);

    List<StudySession> findByUserIdAndStartedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    @Query("""
            select s.userId, sum(s.durationMinutes)
            from StudySession s
            where s.startedAt >= :from
            group by s.userId
            order by sum(s.durationMinutes) desc
            """)
    List<Object[]> rankingFrom(LocalDateTime from);

    @Query("""
            select s.userId, sum(s.durationMinutes)
            from StudySession s
            group by s.userId
            order by sum(s.durationMinutes) desc
            """)
    List<Object[]> rankingAllTime();
}
