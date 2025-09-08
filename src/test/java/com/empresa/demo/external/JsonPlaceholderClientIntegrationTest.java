package com.empresa.demo.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.empresa.demo.model.PostDto;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
@ActiveProfiles("test")
class JsonPlaceholderClientIntegrationTest {
	static MockWebServer server;

	@BeforeAll
	static void beforeAll() throws Exception {
		server = new MockWebServer();
		server.start();
	}

	@AfterAll
	static void afterAll() throws Exception {
		server.shutdown();
	}

	@DynamicPropertySource
	static void registerProps(DynamicPropertyRegistry registry) {
		registry.add("external.api.base-url", () -> server.url("/").toString());
		registry.add("external.api.timeout-seconds", () -> 2);
	}

	@Autowired
	JsonPlaceholderClient client;

	private static MockResponse json(int code, String body) {
		return new MockResponse().setResponseCode(code).addHeader("Content-Type", "application/json").setBody(body);
	}

	private static final String POSTS_JSON = """
			[
			  {"userId": 1, "id": 10, "title": "t-10", "body": "b-10"},
			  {"userId": 2, "id": 20, "title": "t-20", "body": "b-20"}
			]
			""";

	@Test
	void should_return_posts_on_200() {
		// Arrange
		server.enqueue(json(200, POSTS_JSON));

		// Act
		List<PostDto> out = client.fetchPosts();

		// Assert
		assertThat(out).hasSize(2);
		assertThat(out.get(0).getId()).isEqualTo(10);
		assertThat(out.get(1).getTitle()).isEqualTo("t-20");
	}

	@Test
	void should_retry_once_on_5xx_and_then_succeed() {
		int before = server.getRequestCount();

		// 1ª llamada: 500 -> fuerza reintento
		server.enqueue(json(500, "{\"error\":\"boom\"}"));
		// 2ª llamada (reintento): 200
		server.enqueue(json(200, POSTS_JSON));

		List<PostDto> out = client.fetchPosts();

		assertThat(out).hasSize(2);
		assertThat(server.getRequestCount() - before).isEqualTo(2); // se reintentó 1 vez
	}

	@Test
	void should_timeout_and_fallback_to_empty_list() {
		int before = server.getRequestCount();
		
		// Demora mayor al timeout configurado (2s)
		server.enqueue(new MockResponse().setResponseCode(200).setBody(POSTS_JSON).setBodyDelay(3, TimeUnit.SECONDS));

		List<PostDto> out = client.fetchPosts();

		assertThat(out).isEmpty();
		assertThat(server.getRequestCount() - before).isEqualTo(1);
	}
	
	@Test
    void should_map_4xx_to_invalid_request_exception() {
		int before = server.getRequestCount();
		
        server.enqueue(json(400, "{\"error\":\"bad request\"}"));

        assertThrows(InvalidRequestException.class, () -> client.fetchPosts());
        assertThat(server.getRequestCount() - before).isEqualTo(1);
    }

}
