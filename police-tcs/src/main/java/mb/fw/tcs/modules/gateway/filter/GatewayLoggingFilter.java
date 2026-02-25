package mb.fw.tcs.modules.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String globalTraceId = UUID.randomUUID().toString().substring(0, 8);
//		ServerHttpRequest request = exchange.getRequest();
		ServerHttpRequest request = exchange.getRequest().mutate().header("X-Global-Trace-ID", globalTraceId).build();
		String path = request.getPath().value();
		String method = request.getMethodValue();

		// 1. 요청 로깅
		log.info("👉 [REQUEST - {}] {} {} | Headers: {}", globalTraceId, method, path, request.getHeaders());

		// 2. 응답 로깅을 위한 Decorator 설정
		ServerHttpResponse originalResponse = exchange.getResponse();
		DataBufferFactory bufferFactory = originalResponse.bufferFactory();

		ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				if (body instanceof Flux) {
					Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
					return super.writeWith(fluxBody.map(dataBuffer -> {
						byte[] content = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(content);
						// 메모리 누수 방지
						DataBufferUtils.release(dataBuffer);

						String responseBody = new String(content, StandardCharsets.UTF_8);
						log.info("✅ [RESPONSE - {}] {} {} | Status: {} | Body: {}", globalTraceId, method, path,
								getStatusCode(), responseBody);

						return bufferFactory.wrap(content);
					}));
				}
				return super.writeWith(body);
			}
		};

		return chain.filter(exchange.mutate().response(decoratedResponse).build());
	}

	@Override
	public int getOrder() {
		return -1;
	}
}