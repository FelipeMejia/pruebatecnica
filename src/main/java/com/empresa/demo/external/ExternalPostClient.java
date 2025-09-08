package com.empresa.demo.external;

import java.util.List;

import com.empresa.demo.model.PostDto;

public interface ExternalPostClient {
	List<PostDto> fetchPosts();
}
