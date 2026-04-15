package mb.fw.tcs.modules.pics.common.config;

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
import mb.fw.tcs.common.constants.ModuleConfigConstants;
import reactor.netty.http.client.HttpClient;

@Data
@Configuration
@ConfigurationProperties(prefix = ModuleConfigConstants.PICS_API_WEB_PREFIX, ignoreUnknownFields = true)
public class WebClientConfig {
	
	private String apigUrl;
	private int requestTimeoutSeconds = 30;

//	@Bean("picsWebClient")
//	WebClient esbWebClient() {
//		// 대용량 처리를 위한 메모리 제한 해제 (200MB)
////	    ExchangeStrategies strategies = ExchangeStrategies.builder()
////	            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(200 * 1024 * 1024))
////	            .build();
//
//		HttpClient httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000) // 연결 시도 시간 (10초)
//				.responseTimeout(Duration.ofSeconds(requestTimeoutSeconds));
////	            .doOnConnected(conn -> conn
////	                // 개별 패킷 사이의 읽기/쓰기 제한 시간을 0(무제한) 혹은 매우 길게 설정
////	                .addHandlerLast(new ReadTimeoutHandler(0)) 
////	                .addHandlerLast(new WriteTimeoutHandler(0)));
//		return WebClient.builder().baseUrl(apigUrl)
////	            .exchangeStrategies(strategies)
//				.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
//	}
	
	@Bean("picsWebClient")
	WebClient picsWebClient() { // 빈 이름과 메서드 이름을 일치시키는 것이 좋습니다.
	    
	    // 1. 대용량 처리를 위한 메모리 제한 확장 (기존 주석 해제 및 적용)
	    ExchangeStrategies strategies = ExchangeStrategies.builder()
	            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(400 * 1024 * 1024)) // 100MB
	            .build();

	    // 2. HttpClient 설정 (Timeout 및 커넥션 관리)
	    HttpClient httpClient = HttpClient.create()
	            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 연결 시도 (10초로 권장)
	            .responseTimeout(Duration.ofSeconds(requestTimeoutSeconds))
	            // 앞서 발생한 PrematureCloseException 방지를 위한 커넥션 풀 최적화
	            .doOnConnected(conn -> conn
	                .addHandlerLast(new ReadTimeoutHandler(requestTimeoutSeconds)) 
	                .addHandlerLast(new WriteTimeoutHandler(requestTimeoutSeconds)));

	    return WebClient.builder()
	            .baseUrl(apigUrl)
	            .exchangeStrategies(strategies) // 🔥 이 부분이 반드시 포함되어야 합니다!
	            .clientConnector(new ReactorClientHttpConnector(httpClient))
	            .build();
	}

}
