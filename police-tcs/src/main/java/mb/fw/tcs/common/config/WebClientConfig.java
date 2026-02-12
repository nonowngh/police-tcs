package mb.fw.tcs.common.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import reactor.netty.http.client.HttpClient;

@Data
@Configuration
@ConfigurationProperties(prefix = "web.client", ignoreUnknownFields = true)
public class WebClientConfig {
	private String picsUrl;
	
	@Bean("picsWebClient")
	WebClient esbWebClient() {
		// 대용량 처리를 위한 메모리 제한 해제 (200MB)
//	    ExchangeStrategies strategies = ExchangeStrategies.builder()
//	            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(200 * 1024 * 1024))
//	            .build();
	    
	    HttpClient httpClient = HttpClient.create()
	            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // 연결 시도 시간 (10초)
	            .responseTimeout(Duration.ofMinutes(3));           // 전체 응답 대기 시간 (15분으로 상향)
//	            .doOnConnected(conn -> conn
//	                // 개별 패킷 사이의 읽기/쓰기 제한 시간을 0(무제한) 혹은 매우 길게 설정
//	                .addHandlerLast(new ReadTimeoutHandler(0)) 
//	                .addHandlerLast(new WriteTimeoutHandler(0)));
	    return WebClient.builder()
	            .baseUrl(picsUrl)
//	            .exchangeStrategies(strategies)
	            .clientConnector(new ReactorClientHttpConnector(httpClient))
//	            .defaultHeader(InterfaceAuthConstants.AUTH_HEADER, InterfaceAuthConstants.AUTH_KEY)
	            .build();
	}
	
}
