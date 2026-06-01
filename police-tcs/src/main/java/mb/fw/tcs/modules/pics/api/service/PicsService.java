package mb.fw.tcs.modules.pics.api.service;

import java.util.Map;

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

	private static final String ENCRYPT_TARGET_FIELD = "data";

	@Qualifier("picsWebClient")
	private final WebClient picsWebClient;

	private final PicsApiConfig picsApiConfig;
	private final GpkiService gpkiService;
	private final ObjectMapper objectMapper;

	public ResponseEntity<Object> callApi(InterfaceSpec spec, Object requestBody, String picsTransactionId)
			throws Exception {
		Object finalBody;
		// gpki encrypt
		try {
			finalBody = encryptRequestBody(requestBody, spec);
		} catch (Exception e) {
			throw new RuntimeException("요청 데이터 암호화 중 오류가 발생했습니다. -> " + e.getMessage(), e);
		}

		// call apig
		ResponseEntity<String> responseEntity;
		try {
			responseEntity = postWebClientSync(spec, finalBody, picsTransactionId);
			log.info("🟢 pics-api-call 'success' tx-id: {}, status: {}", picsTransactionId,
					responseEntity.getStatusCode());
		} catch (Exception e) {
			log.error("🔴 pics-api-call 'fail' tx-id: {}, error: ", picsTransactionId, e);
			throw new RuntimeException("pics-api 호출 오류 -> " + e.getMessage(), e);
		}

		// gpki decrypt
//		try {
//			responseEntity = decryptResponseBody(responseEntity, spec);
//		} catch (Exception e) {
//			throw new RuntimeException("응답 데이터 복호화 중 오류가 발생했습니다. -> " + e.getMessage(), e);
//		}

		try {
			return decryptResponseBody(responseEntity, spec);
		} catch (Exception e) {
			throw new RuntimeException("응답 데이터 복호화 중 오류가 발생했습니다. -> " + e.getMessage(), e);
		}
	}

	private ResponseEntity<String> postWebClientSync(InterfaceSpec spec, Object requestBody, String picsTransactionId) {

		return picsWebClient.post().uri(uriBuilder -> uriBuilder.path(spec.getApiPath()).build())
				.header(HttpHeaders.HOST.toUpperCase(), "localhost").header(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header(ModuleFieldConstants.PICS_HEADER_API_KEY, spec.getApiKey())
				.header(ModuleFieldConstants.PICS_HEADER_MY_CERT_SERVER_ID, picsApiConfig.getGpkiProp().getMyCertId())
				.header(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID, picsTransactionId)
				.header(ModuleFieldConstants.PICS_HEADER_GPKI_YN, picsApiConfig.isUseGpki() ? "Y" : "N")
				.bodyValue(requestBody).retrieve().toEntity(String.class).block(); // 결과를 받을 때까지 현재 쓰레드를 멈춤(동기화)
	}

	private Object encryptRequestBody(Object body, InterfaceSpec spec) throws Exception {
		if (!picsApiConfig.isUseGpki())
			return body;
		Map<String, Object> bodyMap;
		if (body instanceof String) {
			bodyMap = objectMapper.readValue((String) body, Map.class);
		} else if (body instanceof Map) {
			bodyMap = (Map<String, Object>) body;
		} else {
			bodyMap = objectMapper.convertValue(body, Map.class);
		}
		if (bodyMap.containsKey(ENCRYPT_TARGET_FIELD)) {
			Object targetData = bodyMap.get(ENCRYPT_TARGET_FIELD);
			if (targetData != null) {
				// 내부 데이터가 객체면 문자열로 변환, 문자열이면 그대로 사용
				String plainText = (targetData instanceof String) ? (String) targetData
						: objectMapper.writeValueAsString(targetData);
				String encryptedText = gpkiService.encryptData(plainText, spec.getProviderCertId());
				bodyMap.put(ENCRYPT_TARGET_FIELD, encryptedText);
			}
		}
		return bodyMap;
	}

	private ResponseEntity<Object> decryptResponseBody(ResponseEntity<String> responseEntity, InterfaceSpec spec)
			throws Exception {

		String rawBody = responseEntity.getBody();
		if (rawBody == null || rawBody.isEmpty()) {
			return ResponseEntity.status(responseEntity.getStatusCode()).headers(responseEntity.getHeaders()).build();
		}

		String decryptedJson;
		if (picsApiConfig.isUseGpki()) {
			decryptedJson = gpkiService.decryptData(rawBody, spec.getProviderCertId());
		} else {
			decryptedJson = rawBody;
		}

		Object resultObject = tryParseJson(decryptedJson);

		HttpHeaders headers = new HttpHeaders();
		headers.addAll(responseEntity.getHeaders());
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.remove(HttpHeaders.CONTENT_LENGTH);
		headers.remove(HttpHeaders.TRANSFER_ENCODING);

//        return ResponseEntity.status(responseEntity.getStatusCode())
//                .headers(responseEntity.getHeaders())
//                .body(resultObject);

		return ResponseEntity.status(responseEntity.getStatusCode()).headers(headers).body(resultObject);
	}

	private Object tryParseJson(String text) {
		if (text == null)
			return null;
		String trimmed = text.trim();
		// JSON 객체({})나 배열([]) 형태인 경우에만 ObjectMapper 사용
		if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
			try {
				return objectMapper.readValue(text, Object.class);
			} catch (Exception e) {
				log.warn("JSON 파싱 실패, 원문 반환: {}", e.getMessage());
				return text;
			}
		}
		return text;
	}

}
