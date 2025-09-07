package com.empresa.demo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
	@Value("${external.api.base-url}")
	private String baseUrl;

	@Value("${external.api.timeout-seconds}")
	private int timeoutSeconds;

	@Bean
	public WebClient webClient(WebClient.Builder builder) {

		HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(timeoutSeconds));
		return builder.baseUrl(baseUrl).clientConnector(new ReactorClientHttpConnector(httpClient)).build();
	}
}
