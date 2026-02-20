package mb.fw.tcs.modules.pics.api.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.modules.pics.common.util.GpkiUtil;
import mb.fw.tcs.modules.pics.common.util.ShareGpki;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpkiService {

	private final ShareGpki shareGpki;
	private final String ENCODING_STR = "utf-8";

	private GpkiUtil getUtil() {
		return shareGpki.getGpki();
	}

	public String encryptData(String plainText, String providerCertId) throws Exception {
		if (plainText == null || plainText.isEmpty()) {
			return plainText;
		}
		try {
			byte[] encrypted = getUtil().encrypt(plainText.getBytes(ENCODING_STR), providerCertId);
			byte[] signed = getUtil().sign(encrypted);
			String encoded = getUtil().encode(signed);
			log.debug("Request : after GPKI json => \n" + encoded);
			return encoded;
		} catch (Exception e) {
			log.error("GPKI 암호화 중 오류 발생: {}", e.getMessage());
			throw e;
		}
	}

	public String decryptData(String encText, String providerCertId) throws Exception {
		if (encText == null || encText.isEmpty()) {
			return encText;
		}
		try {
			byte[] decoded = getUtil().decode(encText);
			byte[] validated = getUtil().validate(decoded);
			String decrypted = new String(getUtil().decrypt(validated), ENCODING_STR);
			log.debug("Response : after GPKI json => \n" + decrypted);
			return decrypted;
		} catch (Exception e) {
			log.error("GPKI 암호화 중 오류 발생: {}", e.getMessage());
			throw e;
		}
	}
}
