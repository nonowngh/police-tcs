package mb.fw.tcs.modules.pics.common.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShareGpki {
	@Value("${esb.gpki.myServerId}")
	String myServerId;
	@Value("${esb.gpki.certFilePath}")
	String certFilePath;
	@Value("${esb.gpki.envCertFilePathName}")
	String envCertFilePathName;
	@Value("${esb.gpki.envPrivateKeyFilePathName}")
	String envPrivateKeyFilePathName;
	@Value("${esb.gpki.envPrivateKeyPasswd}")
	String envPrivateKeyPasswd;
	@Value("${esb.gpki.sigCertFilePathName}")
	String sigCertFilePathName;
	@Value("${esb.gpki.sigPrivateKeyFilePathName}")
	String sigPrivateKeyFilePathName;
	@Value("${esb.gpki.sigPrivateKeyPasswd}")
	String sigPrivateKeyPasswd;
	@Value("${esb.gpki.licPath}")
	String gpkiLicPath;
	@Value("${esb.gpki.useLdap}")
	boolean useLdap;
	@Value("${esb.gpki.ldapUrl}")
	String ldapUrl;
	@Value("${esb.gpki.targetServerId}")
	String targetServerId;
	@Getter
	private GpkiUtil gpki;

	@PostConstruct
	public void init() throws Exception {
		log.info("GPKI 엔진 초기화 시작 (Target: {})", targetServerId);
		this.gpki = createGpkiUtil(targetServerId);
		log.info("GPKI 엔진 초기화 완료");
	}

	private GpkiUtil createGpkiUtil(String targetId) throws Exception {
		GpkiUtil g = new GpkiUtil();

		g.setCertFilePath(certFilePath);
		g.setGpkiLicPath(gpkiLicPath);
		g.setEnvCertFilePathName(envCertFilePathName);
		g.setEnvPrivateKeyFilePathName(envPrivateKeyFilePathName);
		g.setEnvPrivateKeyPasswd(envPrivateKeyPasswd);
		g.setIsLDAP(useLdap);
		g.setMyServerId(myServerId);
		g.setSigCertFilePathName(sigCertFilePathName);
		g.setSigPrivateKeyFilePathName(sigPrivateKeyFilePathName);
		g.setSigPrivateKeyPasswd(sigPrivateKeyPasswd);

		g.setTargetServerIdList(targetId);
		g.init(ldapUrl);

		return g;
	}
}