package com.educationalapp.backend.user;

import com.educationalapp.backend.auth.AuthDtos.AuthResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/name")
    public AuthResponse updateName(@PathVariable Long userId, @Valid @RequestBody UpdateNameRequest request) {
        AppUser user = userService.updateName(userId, request.name());
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getCoins());
    }

    public record UpdateNameRequest(@NotBlank @Size(max = 80) String name) {
    }
}
