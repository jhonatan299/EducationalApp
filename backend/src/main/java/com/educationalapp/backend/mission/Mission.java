package com.educationalapp.backend.mission;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;
    private String description;
    private int targetMinutes;
    private int progressMinutes;
    private int rewardCoins;
    private boolean claimed;

    protected Mission() {
    }

    public Mission(Long userId, String title, String description, int targetMinutes, int rewardCoins) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetMinutes = targetMinutes;
        this.progressMinutes = 0;
        this.rewardCoins = rewardCoins;
        this.claimed = false;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getTargetMinutes() {
        return targetMinutes;
    }

    public int getProgressMinutes() {
        return progressMinutes;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public boolean isCompleted() {
        return progressMinutes >= targetMinutes;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public void addProgress(int minutes) {
        this.progressMinutes += minutes;
    }

    public void claim() {
        this.claimed = true;
    }
}
