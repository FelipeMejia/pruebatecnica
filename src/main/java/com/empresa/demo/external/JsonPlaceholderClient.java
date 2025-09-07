package com.empresa.demo.external;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.empresa.demo.model.PostDto;

@Component
public class JsonPlaceholderClient {

	private final WebClient webClient;

	@Autowired
	public JsonPlaceholderClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public List<PostDto> fetchPosts() {
		WebClient.ResponseSpec response = webClient.get().uri("/posts").retrieve();

		return response.bodyToFlux(PostDto.class).collectList().block();
	}

}
