package com.educationalapp.backend.config;

import com.educationalapp.backend.auth.AuthDtos.RegisterRequest;
import com.educationalapp.backend.auth.AuthService;
import com.educationalapp.backend.common.StudyType;
import com.educationalapp.backend.goal.GoalDtos.CreateGoalRequest;
import com.educationalapp.backend.goal.GoalService;
import com.educationalapp.backend.study.StudySessionDtos.ManualStudySessionRequest;
import com.educationalapp.backend.study.StudySessionService;
import com.educationalapp.backend.user.AppUserRepository;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedPrototypeData(AppUserRepository userRepository, AuthService authService,
                                        GoalService goalService, StudySessionService studySessionService) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            var user = authService.register(new RegisterRequest("Estudante Demo", "demo@educational.app", "123456"));

            goalService.create(new CreateGoalRequest(
                    user.userId(),
                    "Estudar Matemática 10h até a prova",
                    "Matemática",
                    600,
                    LocalDate.now().plusDays(14)
            ));
            goalService.create(new CreateGoalRequest(
                    user.userId(),
                    "Revisar História antes do simulado",
                    "História",
                    240,
                    LocalDate.now().plusDays(7)
            ));

            studySessionService.createManualSession(new ManualStudySessionRequest(
                    user.userId(),
                    "Matemática",
                    50,
                    StudyType.EXERCISES,
                    "Funções",
                    "Resolver exercícios de função do primeiro grau",
                    "Revisão de funções e exercícios.",
                    "Melhorei a leitura dos gráficos.",
                    "Ainda erro quando a questão mistura tabela e gráfico.",
                    "Fazer mais 10 exercícios amanhã.",
                    4
            ));
            studySessionService.createManualSession(new ManualStudySessionRequest(
                    user.userId(),
                    "História",
                    30,
                    StudyType.THEORY,
                    "Brasil República",
                    "Ler resumo e organizar linha do tempo",
                    "Resumo sobre Brasil República.",
                    "Entendi melhor a política do café com leite.",
                    "Preciso revisar datas importantes.",
                    "Montar mapa mental.",
                    3
            ));
        };
    }
}
