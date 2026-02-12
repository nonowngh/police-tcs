package mb.fw.tcs.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "pics.api", ignoreUnknownFields = true)
public class PicsApiConfig {

	private String myCertServerId;

}
