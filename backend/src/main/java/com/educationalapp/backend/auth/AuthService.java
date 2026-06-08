package com.educationalapp.backend.auth;

import com.educationalapp.backend.auth.AuthDtos.AuthResponse;
import com.educationalapp.backend.auth.AuthDtos.LoginRequest;
import com.educationalapp.backend.auth.AuthDtos.RegisterRequest;
import com.educationalapp.backend.mission.Mission;
import com.educationalapp.backend.mission.MissionRepository;
import com.educationalapp.backend.user.AppUser;
import com.educationalapp.backend.user.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final MissionRepository missionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository userRepository, MissionRepository missionRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.missionRepository = missionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        AppUser user = userRepository.save(new AppUser(
                request.name(),
                request.email(),
                passwordEncoder.encode(request.password())
        ));
        createDefaultMissions(user.getId());
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return toResponse(user);
    }

    private void createDefaultMissions(Long userId) {
        missionRepository.save(new Mission(userId, "Primeiro foco", "Complete 25 minutos de estudo", 25, 10));
        missionRepository.save(new Mission(userId, "Sessao forte", "Complete 60 minutos de estudo", 60, 25));
        missionRepository.save(new Mission(userId, "Maratona leve", "Complete 120 minutos de estudo", 120, 60));
    }

    private AuthResponse toResponse(AppUser user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getCoins());
    }
}
