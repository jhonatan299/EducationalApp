package com.educationalapp.backend.mission;

import com.educationalapp.backend.mission.MissionDtos.MissionResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/missions")
public class MissionController {
    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping("/user/{userId}")
    public List<MissionResponse> listForUser(@PathVariable Long userId) {
        return missionService.listForUser(userId).stream()
                .map(MissionResponse::from)
                .toList();
    }

    @PostMapping("/{missionId}/claim")
    public MissionResponse claim(@PathVariable Long missionId) {
        return MissionResponse.from(missionService.claim(missionId));
    }
}
