package com.exam.aiquestions.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exam.aiquestions.model.PdfChunk;
import com.exam.aiquestions.service.VectorSearchService;

@RestController
public class SearchController {

    @Autowired
    private VectorSearchService searchService;

    @GetMapping("/search")
    public List<String> search(
            @RequestParam String topic,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String contentTopic,
            @RequestParam(required = false) String roleOrExam) {

        return searchService.search(topic, domain, subject, contentTopic, roleOrExam);
    }
}
