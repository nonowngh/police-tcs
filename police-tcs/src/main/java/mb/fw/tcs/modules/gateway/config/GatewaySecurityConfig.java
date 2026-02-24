package mb.fw.tcs.modules.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity // WebFlux용 어노테이션
public class GatewaySecurityConfig {

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		return http.csrf(csrf -> csrf.disable()) // Gateway 입구에서 CSRF 차단 해제
				.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll() // 모든 요청 통과 (뒤쪽 서비스에서 체크)
				).build();
	}
}