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
import mb.fw.tcs.common.config.GatewayAuthConfig;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GatewayAuthFilter implements GlobalFilter, Ordered {
	
	private final GatewayAuthConfig authConfig;	
	
    private static final String AUTH_HEADER_NAME = "X-Gateway-Key";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

        // 1. 헤더에서 API-Key 추출
        String clientKey = request.getHeaders().getFirst(AUTH_HEADER_NAME);

        // 2. 키 비교 (Constant Time Comparison 권장하나 단순 비교도 성능상 우수)
        if (clientKey == null || !clientKey.equals(authConfig.getApiKey())) {
            log.warn("Unauthorized access attempt! Path: {}, IP: {}", 
                     request.getPath(), request.getRemoteAddress());

            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // 401 에러
            
            // 응답 바디에 메시지를 넣고 싶다면 여기서 처리 가능
            return response.setComplete();
        }

        // 3. 인증 성공 시 로그에 남기거나 다음 필터로 전달
        return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
