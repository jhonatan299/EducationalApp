package com.educationalapp.backend.mission;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByUserIdOrderByIdAsc(Long userId);
}
