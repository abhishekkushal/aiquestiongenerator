package com.exam.aiquestions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.exam.aiquestions.service.PDFService;

@RestController
public class PDFController {
	
	@Autowired
	PDFService pdfService;

	@PostMapping("/upload-pdf")
	public String upload(@RequestParam("file") MultipartFile file) {
	    pdfService.processPdf(file);
        return "PDF processed and stored: " + file.getOriginalFilename();
	}
}
