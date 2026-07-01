package com.educationalapp.backend.study;

import com.educationalapp.backend.study.StudySessionDtos.ManualStudySessionRequest;
import com.educationalapp.backend.study.StudySessionDtos.StudySessionResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/study-sessions")
public class StudySessionController {
    private final StudySessionService studySessionService;

    public StudySessionController(StudySessionService studySessionService) {
        this.studySessionService = studySessionService;
    }

    @PostMapping("/manual")
    public StudySessionResponse createManual(@Valid @RequestBody ManualStudySessionRequest request) {
        return StudySessionResponse.from(studySessionService.createManualSession(request));
    }

    @GetMapping("/user/{userId}")
    public List<StudySessionResponse> listForUser(@PathVariable Long userId) {
        return studySessionService.listForUser(userId).stream()
                .map(StudySessionResponse::from)
                .toList();
    }

    @DeleteMapping("/{sessionId}")
    public void delete(@PathVariable Long sessionId) {
        studySessionService.delete(sessionId);
    }
}
