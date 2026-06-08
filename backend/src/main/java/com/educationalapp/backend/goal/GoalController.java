package com.educationalapp.backend.goal;

import com.educationalapp.backend.goal.GoalDtos.CreateGoalRequest;
import com.educationalapp.backend.goal.GoalDtos.GoalResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public GoalResponse create(@Valid @RequestBody CreateGoalRequest request) {
        return GoalResponse.from(goalService.create(request));
    }

    @GetMapping("/user/{userId}")
    public List<GoalResponse> listForUser(@PathVariable Long userId) {
        return goalService.listForUser(userId).stream()
                .map(GoalResponse::from)
                .toList();
    }
}
