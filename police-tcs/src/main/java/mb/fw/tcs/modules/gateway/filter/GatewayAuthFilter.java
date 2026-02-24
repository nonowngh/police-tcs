package mb.fw.tcs.modules.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.config.GatewayConfig;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayAuthFilter implements GlobalFilter, Ordered {

	private final GatewayConfig gatewayConfig;

//    private static final String AUTH_HEADER_NAME = "X-Gateway-Key";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		String clientKey = request.getHeaders().getFirst(gatewayConfig.getAuthHeaderName());

		if (clientKey == null || !clientKey.equals(gatewayConfig.getAuthKey())) {
			log.warn("Unauthorized access attempt! Path: {}, IP: {}", request.getPath(), request.getRemoteAddress());

			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401 에러

			return response.setComplete();
		}

		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
