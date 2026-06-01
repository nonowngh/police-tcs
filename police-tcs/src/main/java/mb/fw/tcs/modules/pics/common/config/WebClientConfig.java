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

	@Bean("picsWebClient")
	WebClient picsWebClient() { 
	    
	    // 대용량 처리를 위한 메모리 제한 확장
	    ExchangeStrategies strategies = ExchangeStrategies.builder()
	            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(400 * 1024 * 1024))
	            .build();

	    // HttpClient 설정 (Timeout 및 커넥션 관리)
	    HttpClient httpClient = HttpClient.create()
	            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
	            .responseTimeout(Duration.ofSeconds(requestTimeoutSeconds))
	            .doOnConnected(conn -> conn
	                .addHandlerLast(new ReadTimeoutHandler(requestTimeoutSeconds)) 
	                .addHandlerLast(new WriteTimeoutHandler(requestTimeoutSeconds)));

	    return WebClient.builder()
	            .baseUrl(apigUrl)
	            .exchangeStrategies(strategies)
	            .clientConnector(new ReactorClientHttpConnector(httpClient))
	            .build();
	}

}
