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
	private String certFilePath;
	private String envCertFilePathName;
	private String envPrivateKeyFilePathName;
	private String envPrivateKeyPasswd;
	private String sigCertFilePathName;
	private String sigPrivateKeyFilePathName;
	private String sigPrivateKeyPasswd;
	private String gpkiLicPath;
	private boolean useLdap = true;
	private String ldapUrl;
	private String targetCertId = "";

	private boolean useGpki = false;

	@PostConstruct
	public void init() {
		log.info("=================================================");
		log.info(" ✅ [Module Enabled] PicsApi Module is Active!");
		log.info(" 🌟 PicsApi Use Gpki: {}", useGpki);
		log.info(" 🌟 PicsApi my-cert-server-id: {}", myCertId);
		log.info(" 🌟 PicsApi cert-file-path: {}", certFilePath);
		log.info(" 🌟 PicsApi env-cert-file-path-name: {}", envCertFilePathName);
		log.info(" 🌟 PicsApi env-private-key-file-path-name: {}", envPrivateKeyFilePathName);
		log.info(" 🌟 PicsApi env-private-key-passwd: {}", envPrivateKeyPasswd);
		log.info(" 🌟 PicsApi sig-cert-file-path-name: {}", sigCertFilePathName);
		log.info(" 🌟 PicsApi sig-private-key-file-path-name: {}", sigPrivateKeyFilePathName);
		log.info(" 🌟 PicsApi sig-private-key-passwd: {}", sigPrivateKeyPasswd);
		log.info(" 🌟 PicsApi gpki-lic-path: {}", gpkiLicPath);
		log.info(" 🌟 PicsApi use-ldap: {}", useLdap);
		log.info(" 🌟 PicsApi ldap-url: {}", ldapUrl);
		log.info(" 🌟 PicsApi target-cert-id: {}", targetCertId);
		log.info("=================================================");
	}

}
