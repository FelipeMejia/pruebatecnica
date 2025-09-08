package com.empresa.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.empresa.demo.external.ExternalPostClient;
import com.empresa.demo.model.PostDto;

@Service
public class ExternalPostService {
	private final ExternalPostClient client;

	public ExternalPostService(ExternalPostClient client) {
		this.client = client;
	}

	public List<PostDto> getPosts() {
		var posts = client.fetchPosts();

		return posts;
	}
}
