package mb.fw.tcs.common.config;

import javax.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ModuleConfigConstants;
import mb.fw.tcs.modules.pics.common.config.GpkiProp;
import mb.fw.tcs.modules.pics.common.config.WebClientConfig;

@Slf4j
@Data
@Configuration
@ComponentScan(basePackages = { ModuleConfigConstants.PICS_API_PACKAGE, ModuleConfigConstants.PICS_COMMON_PACKAGE })
@ConfigurationProperties(prefix = ModuleConfigConstants.PICS_API_PREFIX, ignoreUnknownFields = true)
@ConditionalOnProperty(prefix = ModuleConfigConstants.PICS_API_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = false)
public class PicsApiConfig {

	private boolean useGpki = false;

	private final GpkiProp gpkiProp;
	private final WebClientConfig webClientConfig;

	public PicsApiConfig(GpkiProp gpkiProp, WebClientConfig webClientConfig) {
		this.gpkiProp = gpkiProp;
		this.webClientConfig = webClientConfig;
	}
	
	@PostConstruct
	public void init() {
		log.info("=================================================");
		log.info(" ✅ [Module Enabled] PicsApi Module is Active!");
		log.info(" 🌟 PicsApi Web APIG URL: {}", webClientConfig.getApigUrl());
		log.info(" 🌟 PicsApi Web Request Timeout Seconds: {}", webClientConfig.getRequestTimeoutSeconds());
		log.info(" 🌟 PicsApi Use Gpki: {}", useGpki);
		if (useGpki) {
			log.info(" 🌟 PicsApi Gpki my-cert-id: {}", gpkiProp.getMyCertId());
			log.info(" 🌟 PicsApi Gpki cert-file-path: {}", gpkiProp.getCertFilePath());
			log.info(" 🌟 PicsApi Gpki env-cert-file-path-name: {}", gpkiProp.getEnvCertFilePathName());
			log.info(" 🌟 PicsApi Gpki env-private-key-file-path-name: {}", gpkiProp.getEnvPrivateKeyFilePathName());
			log.info(" 🌟 PicsApi Gpki env-private-key-passwd: {}", gpkiProp.getEnvPrivateKeyPasswd());
			log.info(" 🌟 PicsApi Gpki sig-cert-file-path-name: {}", gpkiProp.getSigCertFilePathName());
			log.info(" 🌟 PicsApi Gpki sig-private-key-file-path-name: {}", gpkiProp.getSigPrivateKeyFilePathName());
			log.info(" 🌟 PicsApi Gpki sig-private-key-passwd: {}", gpkiProp.getSigPrivateKeyPasswd());
			log.info(" 🌟 PicsApi Gpki gpki-lic-path: {}", gpkiProp.getGpkiLicPath());
			log.info(" 🌟 PicsApi Gpki use-ldap: {}", gpkiProp.isUseLdap());
			log.info(" 🌟 PicsApi Gpki ldap-url: {}", gpkiProp.getLdapUrl());
//			log.info(" 🌟 PicsApi Gpki target-cert-id: {}", gpkiProp.getTargetCertId());
		}
		log.info("=================================================");
	}
	
}
