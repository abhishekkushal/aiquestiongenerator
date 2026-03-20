package com.exam.aiquestions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.aiquestions.service.QuestionService;

@RestController
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/generate-mcq")
    public String generate(@RequestParam String topic) {

        return questionService.generateMCQ(topic);
    }
}
