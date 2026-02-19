package mb.fw.tcs.common.config;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Configuration
@ComponentScan(basePackages = "mb.fw.tcs.modules.gateway")
@ConfigurationProperties(prefix = "module.gateway", ignoreUnknownFields = true)
@ConditionalOnProperty(prefix = "module.gateway", name = "enabled", havingValue = "true", matchIfMissing = false)
public class GatewayConfig {

	private String authKey = "my-secret-api-key-2026";
	
	private String authHeaderName = "X-Gateway-Key";
	
	@PostConstruct
    public void init() {
        log.info("=================================================");
        log.info(" ✅ [Module Enabled] Gateway Module is Active!");
        log.info(" 🌟 Gateway Auth Header: {}", authHeaderName);
        log.info(" 🌟 Gateway Auth Key: {}", authKey);
        log.info("=================================================");
    }
}
