package com.educationalapp.backend.mission;

public final class MissionDtos {
    private MissionDtos() {
    }

    public record MissionResponse(
            Long id,
            Long userId,
            String title,
            String description,
            int targetMinutes,
            int progressMinutes,
            int rewardCoins,
            boolean completed,
            boolean claimed
    ) {
        public static MissionResponse from(Mission mission) {
            return new MissionResponse(
                    mission.getId(),
                    mission.getUserId(),
                    mission.getTitle(),
                    mission.getDescription(),
                    mission.getTargetMinutes(),
                    mission.getProgressMinutes(),
                    mission.getRewardCoins(),
                    mission.isCompleted(),
                    mission.isClaimed()
            );
        }
    }
}
