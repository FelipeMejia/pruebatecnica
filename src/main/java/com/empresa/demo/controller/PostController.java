package com.empresa.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empresa.demo.model.PostDto;
import com.empresa.demo.service.ExternalPostService;

@RestController
@RequestMapping("/external")
public class PostController {
	private final ExternalPostService service;

	public PostController(ExternalPostService service) {
		this.service = service;
	}

	@GetMapping("/posts")
	public List<PostDto> getPosts() {
		return service.getPosts();
	}
}
