package mb.fw.tcs.modules.pics.api.service;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.config.PicsApiConfig;
import mb.fw.tcs.common.constants.ModuleFieldConstants;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpec;

@Slf4j
@Service
@RequiredArgsConstructor
public class PicsService {

	@Qualifier("picsWebClient")
	private final WebClient picsWebClient;

	private final PicsApiConfig picsApiConfig;
	private final GpkiService gpkiService;
	private final ObjectMapper objectMapper;

	public ResponseEntity<Object> callApi(InterfaceSpec spec, Object requestBody) throws Exception {
		String picsTransactionId = MDC.get(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID);
		Object finalBody;
		// gpki encrypt
		try {
			finalBody = encryptRequestBody(requestBody, spec);
		} catch (Exception e) {
			throw new RuntimeException("요청 데이터 암호화 중 오류가 발생했습니다.", e);
		}

		// call apig
		ResponseEntity<Object> responseEntity;
		try {
			responseEntity = postWebClientSync(spec, finalBody);
			log.info("[pics-api-success] tx-id: {}, status: {}", picsTransactionId, responseEntity.getStatusCode());
		} catch (Exception e) {
			log.error("[pics-api-error] tx-id: {}, message: {}", picsTransactionId, e.getMessage());
			throw new RuntimeException("pics-api 호출 오류", e);
		}

		// gpki decrypt
		try {
			responseEntity = decryptResponseBody(responseEntity, spec);
		} catch (Exception e) {
			throw new RuntimeException("응답 데이터 복호화 중 오류가 발생했습니다.", e);
		}

		return responseEntity;
	}

	private ResponseEntity<Object> postWebClientSync(InterfaceSpec spec, Object requestBody) {
		String picsTransactionId = MDC.get(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID);

		return picsWebClient.post().uri(uriBuilder -> uriBuilder.path(spec.getApiPath()).build())
				.header(HttpHeaders.HOST.toUpperCase(), "localhost")
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header(ModuleFieldConstants.PICS_HEADER_API_KEY, spec.getApiKey())
				.header(ModuleFieldConstants.PICS_HEADER_MY_CERT_SERVER_ID, picsApiConfig.getMyCertId())
				.header(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID, picsTransactionId)
				.header(ModuleFieldConstants.PICS_HEADER_GPKI_YN, picsApiConfig.isUseGpki() ? "Y" : "N")
				.bodyValue(requestBody).retrieve().toEntity(Object.class).block(); // 결과를 받을 때까지 현재 쓰레드를 멈춤(동기화)
	}

	private Object encryptRequestBody(Object body, InterfaceSpec spec) throws Exception {
		if (!picsApiConfig.isUseGpki())
			return body;
		return gpkiService.encryptData((body instanceof String) ? (String) body : objectMapper.writeValueAsString(body),
				spec.getProviderCertId());
	}

	private ResponseEntity<Object> decryptResponseBody(ResponseEntity<Object> responseEntity, InterfaceSpec spec)
			throws Exception {
		if (!picsApiConfig.isUseGpki() || responseEntity.getBody() != null)
			return responseEntity;
		String encryptedResBody = responseEntity.getBody().toString();
		String decryptedJson = gpkiService.decryptData(encryptedResBody, spec.getProviderCertId());
		return ResponseEntity.status(responseEntity.getStatusCode()).headers(responseEntity.getHeaders())
				.body(objectMapper.readValue(decryptedJson, Object.class));
	}

}
