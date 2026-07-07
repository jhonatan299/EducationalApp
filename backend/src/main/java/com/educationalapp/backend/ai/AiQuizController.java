package com.educationalapp.backend.ai;

import com.educationalapp.backend.ai.AiQuizDtos.GenerateQuizRequest;
import com.educationalapp.backend.ai.AiQuizDtos.GeneratedQuizResponse;
import com.educationalapp.backend.ai.AiQuizDtos.QuestionListRequest;
import com.educationalapp.backend.ai.AiQuizDtos.QuestionListResponse;
import com.educationalapp.backend.ai.AiQuizDtos.RevisionRoadmapRequest;
import com.educationalapp.backend.ai.AiQuizDtos.RevisionRoadmapResponse;
import com.educationalapp.backend.ai.AiQuizDtos.SaveQuestionsRequest;
import com.educationalapp.backend.ai.AiQuizDtos.SavedQuestionResponse;
import com.educationalapp.backend.ai.AiQuizDtos.UpdateQuestionRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiQuizController {
    private final AiQuizService aiQuizService;

    public AiQuizController(AiQuizService aiQuizService) {
        this.aiQuizService = aiQuizService;
    }

    @PostMapping("/quiz-from-text")
    public GeneratedQuizResponse generateQuizFromText(@Valid @RequestBody GenerateQuizRequest request) {
        return aiQuizService.generateQuizFromText(request);
    }

    @PostMapping("/revision-roadmap")
    public RevisionRoadmapResponse generateRevisionRoadmap(@Valid @RequestBody RevisionRoadmapRequest request) {
        return aiQuizService.generateRevisionRoadmap(request);
    }

    @PostMapping("/questions")
    public List<SavedQuestionResponse> saveQuestions(@RequestBody SaveQuestionsRequest request) {
        return aiQuizService.saveGeneratedQuestions(request);
    }

    @GetMapping("/questions")
    public List<SavedQuestionResponse> listQuestions(@RequestParam(required = false) String query) {
        return aiQuizService.listQuestions(query);
    }

    @PutMapping("/questions/{id}")
    public SavedQuestionResponse updateQuestion(@PathVariable Long id, @RequestBody UpdateQuestionRequest request) {
        return aiQuizService.updateQuestion(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        aiQuizService.deleteQuestion(id);
    }

    @GetMapping("/question-lists")
    public List<QuestionListResponse> listQuestionLists() {
        return aiQuizService.listQuestionLists();
    }

    @PostMapping("/question-lists")
    public QuestionListResponse createQuestionList(@Valid @RequestBody QuestionListRequest request) {
        return aiQuizService.createQuestionList(request);
    }

    @PutMapping("/question-lists/{listId}")
    public QuestionListResponse renameQuestionList(@PathVariable Long listId, @Valid @RequestBody QuestionListRequest request) {
        return aiQuizService.renameQuestionList(listId, request);
    }

    @DeleteMapping("/question-lists/{listId}")
    public void deleteQuestionList(@PathVariable Long listId) {
        aiQuizService.deleteQuestionList(listId);
    }

    @PostMapping("/question-lists/{listId}/questions/{questionId}")
    public QuestionListResponse addQuestionToList(@PathVariable Long listId, @PathVariable Long questionId) {
        return aiQuizService.addQuestionToList(listId, questionId);
    }

    @DeleteMapping("/question-lists/{listId}/questions/{questionId}")
    public QuestionListResponse removeQuestionFromList(@PathVariable Long listId, @PathVariable Long questionId) {
        return aiQuizService.removeQuestionFromList(listId, questionId);
    }
}
