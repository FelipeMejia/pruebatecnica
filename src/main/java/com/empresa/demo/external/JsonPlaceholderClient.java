package com.empresa.demo.external;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.empresa.demo.model.PostDto;

import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class JsonPlaceholderClient implements ExternalPostClient {
	private static final Logger log = LoggerFactory.getLogger(JsonPlaceholderClient.class);

	private final WebClient webClient;

	@Autowired
	public JsonPlaceholderClient(WebClient webClient) {
		this.webClient = webClient;
	}

	private boolean isRetryable(Throwable ex) {
		if (ex instanceof ServerErrorException) {
			return true;
		}
		if (ex instanceof WebClientResponseException w && w.getStatusCode().is5xxServerError()) {
			return true;
		}
		return false;
	}

	public List<PostDto> fetchPosts() {
		WebClient.ResponseSpec response = webClient.get().uri("/posts").retrieve();

		return response.onStatus(HttpStatusCode::is4xxClientError,
				resp -> resp.bodyToMono(String.class).defaultIfEmpty("").flatMap(body -> {
					log.warn("Petición inválida ({}): {}", resp.statusCode().value(), body);
					return Mono.error(new InvalidRequestException(
							"Client error " + resp.statusCode().value() + " calling /posts: " + body));
				})).onStatus(HttpStatusCode::is5xxServerError,
						resp -> resp.bodyToMono(String.class).defaultIfEmpty("").flatMap(body -> {
							log.error("Error de servidor ({}): {}", resp.statusCode().value(), body);
							return Mono.error(new ServerErrorException(
									"Server error " + resp.statusCode().value() + " calling /posts: " + body));
						}))
				.bodyToFlux(PostDto.class).collectList().retryWhen(Retry.max(1).filter(this::isRetryable))
				.onErrorResume(throwable -> {
					if (throwable instanceof TimeoutException || throwable instanceof ReadTimeoutException) {
						log.warn(
								"Tiempo de espera excedido al llamar a /posts. Devolviendo lista vacía como alternativa.");
						return Mono.just(Collections.emptyList());
					}

					if (throwable instanceof ServerErrorException || (throwable instanceof WebClientResponseException w
							&& w.getStatusCode().is5xxServerError())) {
						log.warn(
								"Error 5xx persistente al llamar a /posts tras reintento. Devolviendo lista vacía como fallback.");
						return Mono.just(Collections.emptyList());
					}

					// // Otros errores: propagar
					return Mono.error(throwable);
				}).block();
	}

}
