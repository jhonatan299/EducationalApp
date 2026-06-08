package com.educationalapp.backend.goal;

import com.educationalapp.backend.goal.GoalDtos.CreateGoalRequest;
import com.educationalapp.backend.user.UserService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GoalService {
    private final GoalRepository goalRepository;
    private final UserService userService;

    public GoalService(GoalRepository goalRepository, UserService userService) {
        this.goalRepository = goalRepository;
        this.userService = userService;
    }

    public Goal create(CreateGoalRequest request) {
        userService.getUser(request.userId());
        return goalRepository.save(new Goal(
                request.userId(),
                request.title(),
                request.subject(),
                request.targetMinutes(),
                request.dueDate()
        ));
    }

    public List<Goal> listForUser(Long userId) {
        return goalRepository.findByUserIdOrderByDueDateAsc(userId);
    }
}
