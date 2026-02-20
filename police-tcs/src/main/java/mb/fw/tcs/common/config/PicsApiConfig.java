package mb.fw.tcs.common.config;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ModuleConfigConstants;

@Slf4j
@Data
@Configuration
@ComponentScan(basePackages = { ModuleConfigConstants.PICS_API_PACKAGE, ModuleConfigConstants.PICS_COMMON_PACKAGE })
@ConfigurationProperties(prefix = ModuleConfigConstants.PICS_API_PREFIX, ignoreUnknownFields = true)
@ConditionalOnProperty(prefix = ModuleConfigConstants.PICS_API_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = false)
public class PicsApiConfig {

	private String myCertId;

	private boolean useGpki = false;

	@PostConstruct
	public void init() {
		log.info("=================================================");
		log.info(" ✅ [Module Enabled] PicsApi Module is Active!");
		log.info(" 🌟 PicsApi My Cert Server Id: {}", myCertId);
		log.info(" 🌟 PicsApi Use Gpki: {}", useGpki);
		log.info("=================================================");
	}

}
