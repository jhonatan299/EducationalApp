package com.educationalapp.backend.mission;

import com.educationalapp.backend.user.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MissionService {
    private final MissionRepository missionRepository;
    private final UserService userService;

    public MissionService(MissionRepository missionRepository, UserService userService) {
        this.missionRepository = missionRepository;
        this.userService = userService;
    }

    public List<Mission> listForUser(Long userId) {
        return missionRepository.findByUserIdOrderByIdAsc(userId);
    }

    @Transactional
    public Mission claim(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Missão não encontrada: " + missionId));

        if (!mission.isCompleted()) {
            throw new IllegalArgumentException("A missão ainda não foi concluída");
        }
        if (mission.isClaimed()) {
            throw new IllegalArgumentException("Missão já resgatada");
        }

        mission.claim();
        userService.addCoins(mission.getUserId(), mission.getRewardCoins(), "Recompensa da missão: " + mission.getTitle());
        return mission;
    }
}
