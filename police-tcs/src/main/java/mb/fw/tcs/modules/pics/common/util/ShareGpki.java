package mb.fw.tcs.modules.pics.common.util;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.config.PicsApiConfig;

@Slf4j
@Component
public class ShareGpki {

	private PicsApiConfig picsApiConfig;

	public ShareGpki(PicsApiConfig picsApiConfig) {
		this.picsApiConfig = picsApiConfig;
	}

	@Getter
	private GpkiUtil gpki;

	@PostConstruct
	public void init() throws Exception {
		if (!picsApiConfig.isUseGpki())
			return;
		log.info("GPKI 초기화 시작 (Target: {})", picsApiConfig.getGpkiProp().getTargetCertId());
		this.gpki = createGpkiUtil(picsApiConfig.getGpkiProp().getTargetCertId());
		log.info("GPKI 초기화 완료");
	}

	private GpkiUtil createGpkiUtil(String targetId) throws Exception {
		GpkiUtil g = new GpkiUtil();

		g.setCertFilePath(picsApiConfig.getGpkiProp().getCertFilePath());
		g.setGpkiLicPath(picsApiConfig.getGpkiProp().getGpkiLicPath());
		g.setEnvCertFilePathName(picsApiConfig.getGpkiProp().getEnvCertFilePathName());
		g.setEnvPrivateKeyFilePathName(picsApiConfig.getGpkiProp().getEnvPrivateKeyFilePathName());
		g.setEnvPrivateKeyPasswd(picsApiConfig.getGpkiProp().getEnvPrivateKeyPasswd());
		g.setIsLDAP(picsApiConfig.getGpkiProp().isUseLdap());
		g.setMyServerId(picsApiConfig.getGpkiProp().getMyCertId());
		g.setSigCertFilePathName(picsApiConfig.getGpkiProp().getSigCertFilePathName());
		g.setSigPrivateKeyFilePathName(picsApiConfig.getGpkiProp().getSigPrivateKeyFilePathName());
		g.setSigPrivateKeyPasswd(picsApiConfig.getGpkiProp().getSigPrivateKeyPasswd());

		g.setTargetServerIdList(targetId);
		g.init(picsApiConfig.getGpkiProp().getLdapUrl());

		return g;
	}
}