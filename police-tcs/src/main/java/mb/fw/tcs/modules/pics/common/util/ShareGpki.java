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
		if(!picsApiConfig.isUseGpki()) return;
		log.info("GPKI 엔진 초기화 시작 (Target: {})", picsApiConfig.getTargetCertId());
		this.gpki = createGpkiUtil(picsApiConfig.getTargetCertId());
		log.info("GPKI 엔진 초기화 완료");
	}

	private GpkiUtil createGpkiUtil(String targetId) throws Exception {
		GpkiUtil g = new GpkiUtil();

		g.setCertFilePath(picsApiConfig.getCertFilePath());
		g.setGpkiLicPath(picsApiConfig.getGpkiLicPath());
		g.setEnvCertFilePathName(picsApiConfig.getEnvCertFilePathName());
		g.setEnvPrivateKeyFilePathName(picsApiConfig.getEnvPrivateKeyFilePathName());
		g.setEnvPrivateKeyPasswd(picsApiConfig.getEnvPrivateKeyPasswd());
		g.setIsLDAP(picsApiConfig.isUseLdap());
		g.setMyServerId(picsApiConfig.getMyCertId());
		g.setSigCertFilePathName(picsApiConfig.getSigCertFilePathName());
		g.setSigPrivateKeyFilePathName(picsApiConfig.getSigPrivateKeyFilePathName());
		g.setSigPrivateKeyPasswd(picsApiConfig.getSigPrivateKeyPasswd());

		g.setTargetServerIdList(targetId);
		g.init(picsApiConfig.getLdapUrl());

		return g;
	}
}