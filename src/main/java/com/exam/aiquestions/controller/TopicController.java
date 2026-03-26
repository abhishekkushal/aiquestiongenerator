package com.exam.aiquestions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exam.aiquestions.model.Topic;
import com.exam.aiquestions.repository.TopicRepository;

import java.util.List;

@RestController
public class TopicController {

    @Autowired
    private TopicRepository topicRepository;

    @GetMapping("/get-topics")
    public List<Topic> getTopics() {
        return topicRepository.findAll();
    }
}
