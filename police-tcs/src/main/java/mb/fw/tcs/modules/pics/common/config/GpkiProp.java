package mb.fw.tcs.modules.pics.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import mb.fw.tcs.common.constants.ModuleConfigConstants;

@Data
@Configuration
@ConfigurationProperties(prefix = ModuleConfigConstants.PICS_API_GPKI_PREFIX, ignoreUnknownFields = true)
public class GpkiProp {
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
}
