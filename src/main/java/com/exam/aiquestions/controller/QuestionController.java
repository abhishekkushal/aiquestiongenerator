package com.exam.aiquestions.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.aiquestions.model.QuestionType;
import com.exam.aiquestions.model.ReviewStatus;
import com.exam.aiquestions.service.QuestionService;

@RestController
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/generate-questions")
    public Map<String, Object> generate(
            @RequestParam String topic,
            @RequestParam(defaultValue = "MCQ") QuestionType questionType) {

        return questionService.generateQuestions(topic, questionType);
    }

    @GetMapping("/generate-mcq")
    public Map<String, Object> generateMcq(@RequestParam String topic) {
        return questionService.generateQuestions(topic, QuestionType.MCQ);
    }

    @GetMapping("/questions")
    public List<Map<String, Object>> listQuestions(
            @RequestParam(required = false) QuestionType questionType,
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(required = false) String topic) {

        return questionService.listQuestions(questionType, reviewStatus, topic);
    }

    @PostMapping("/questions/{id}/review")
    public Map<String, Object> reviewQuestion(
            @PathVariable Long id,
            @RequestParam ReviewStatus reviewStatus,
            @RequestParam(defaultValue = "Teacher") String reviewedBy) {

        return questionService.reviewQuestion(id, reviewStatus, reviewedBy);
    }
}
