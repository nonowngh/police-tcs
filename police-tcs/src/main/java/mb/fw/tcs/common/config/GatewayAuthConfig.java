package mb.fw.tcs.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "gateway.auth", ignoreUnknownFields = true)
public class GatewayAuthConfig {

	private String apiKey = "my-secret-api-key-2026";
}
