package com.educationalapp.backend.config;

import com.educationalapp.backend.auth.AuthDtos.RegisterRequest;
import com.educationalapp.backend.auth.AuthService;
import com.educationalapp.backend.goal.GoalDtos.CreateGoalRequest;
import com.educationalapp.backend.goal.GoalService;
import com.educationalapp.backend.study.StudySessionDtos.ManualStudySessionRequest;
import com.educationalapp.backend.study.StudySessionService;
import com.educationalapp.backend.common.StudyType;
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
            GoalService goalService,
            StudySessionService studySessionService
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

            var luna = authService.register(new RegisterRequest("Luna", "luna@educational.app", "123456"));
            var rafa = authService.register(new RegisterRequest("Rafa", "rafa@educational.app", "123456"));
            var bia = authService.register(new RegisterRequest("Bia", "bia@educational.app", "123456"));
            var theo = authService.register(new RegisterRequest("Theo", "theo@educational.app", "123456"));

            seedSession(studySessionService, user.userId(), "Matematica", 55);
            seedSession(studySessionService, user.userId(), "Historia", 35);
            seedSession(studySessionService, luna.userId(), "Matematica", 95);
            seedSession(studySessionService, luna.userId(), "Fisica", 45);
            seedSession(studySessionService, rafa.userId(), "Quimica", 70);
            seedSession(studySessionService, rafa.userId(), "Matematica", 50);
            seedSession(studySessionService, bia.userId(), "Biologia", 80);
            seedSession(studySessionService, theo.userId(), "Historia", 65);
        };
    }

    private void seedSession(StudySessionService studySessionService, Long userId, String subject, int minutes) {
        studySessionService.createManualSession(new ManualStudySessionRequest(
                userId,
                subject,
                minutes,
                StudyType.EXERCISES,
                "Sessao demo para ranking",
                "Popular ranking inicial",
                "Dados criados automaticamente para testar ranking real",
                "Resumo demo",
                null,
                null,
                4,
                null,
                null
        ));
    }
}
