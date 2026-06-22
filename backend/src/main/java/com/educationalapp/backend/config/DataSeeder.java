package com.educationalapp.backend.config;

import com.educationalapp.backend.auth.AuthDtos.RegisterRequest;
import com.educationalapp.backend.auth.AuthService;
import com.educationalapp.backend.goal.GoalDtos.CreateGoalRequest;
import com.educationalapp.backend.goal.GoalService;
import com.educationalapp.backend.user.AppUserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedPrototypeData(
            AppUserRepository userRepository,
            AuthService authService,
            GoalService goalService
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            var user = authService.register(
                    new RegisterRequest("Estudante Demo", "demo@educational.app", "123456")
            );

            goalService.create(new CreateGoalRequest(
                    user.userId(),
                    "Estudar Matematica 10h ate a prova",
                    "Matematica",
                    600,
                    LocalDate.now().plusDays(14)
            ));
            goalService.create(new CreateGoalRequest(
                    user.userId(),
                    "Revisar Historia antes do simulado",
                    "Historia",
                    240,
                    LocalDate.now().plusDays(7)
            ));
        };
    }
}
