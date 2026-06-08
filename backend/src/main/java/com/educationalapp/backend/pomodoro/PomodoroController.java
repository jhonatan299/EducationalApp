package com.educationalapp.backend.pomodoro;

import com.educationalapp.backend.pomodoro.PomodoroDtos.CompletePomodoroRequest;
import com.educationalapp.backend.pomodoro.PomodoroDtos.PomodoroResponse;
import com.educationalapp.backend.pomodoro.PomodoroDtos.StartPomodoroRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pomodoros")
public class PomodoroController {
    private final PomodoroService pomodoroService;

    public PomodoroController(PomodoroService pomodoroService) {
        this.pomodoroService = pomodoroService;
    }

    @PostMapping
    public PomodoroResponse start(@Valid @RequestBody StartPomodoroRequest request) {
        return PomodoroResponse.from(pomodoroService.start(request));
    }

    @PostMapping("/{id}/pause")
    public PomodoroResponse pause(@PathVariable Long id) {
        return PomodoroResponse.from(pomodoroService.pause(id));
    }

    @PostMapping("/{id}/resume")
    public PomodoroResponse resume(@PathVariable Long id) {
        return PomodoroResponse.from(pomodoroService.resume(id));
    }

    @PostMapping("/{id}/extend")
    public PomodoroResponse extend(@PathVariable Long id) {
        return PomodoroResponse.from(pomodoroService.extendFiveMinutes(id));
    }

    @PostMapping("/{id}/complete")
    public PomodoroResponse complete(@PathVariable Long id, @RequestBody CompletePomodoroRequest request) {
        return PomodoroResponse.from(pomodoroService.complete(id, request.notes()));
    }

    @GetMapping("/user/{userId}")
    public List<PomodoroResponse> listForUser(@PathVariable Long userId) {
        return pomodoroService.listForUser(userId).stream()
                .map(PomodoroResponse::from)
                .toList();
    }
}
