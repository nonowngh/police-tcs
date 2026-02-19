package mb.fw.tcs.modules.pics.api.service;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.config.PicsApiConfig;
import mb.fw.tcs.common.constants.ModuleFieldConstants;
import mb.fw.tcs.modules.pics.spec.InterfaceSpec;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PicsService {

	@Qualifier("picsWebClient")
	private final WebClient picsWebClient;

	private final PicsApiConfig picsApiConfig;

	public Mono<ResponseEntity<Object>> callApig(InterfaceSpec spec, Object requestBody) {

		String picsTransactionId = MDC.get(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID);
		String apikey = spec.getApiKey();
		String myCertServerId = picsApiConfig.getMyCertId();
		String path = spec.getApiPath();
		
		if(picsApiConfig.isUseGpki()) {
			
		}

		return picsWebClient.post().uri(uriBuilder -> uriBuilder.path(path).build())
				.header(HttpHeaders.HOST.toUpperCase(), "")
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header(ModuleFieldConstants.PICS_HEADER_API_KEY, apikey)
				.header(ModuleFieldConstants.PICS_HEADER_MY_CERT_SERVER_ID, myCertServerId)
				.header(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID, picsTransactionId)
				.header(ModuleFieldConstants.PICS_HEADER_GPKI_YN, "Y")
				.bodyValue(requestBody)
				.retrieve().toEntity(Object.class)
				.doOnNext(res -> log.info("[pics-api-success] tx-id: {}, status: {}", picsTransactionId, res.getStatusCode()))
				.onErrorMap(e -> {
					log.error("[pics-api-error] tx-id: {}, message: {}", picsTransactionId, e.getMessage());
					return new RuntimeException("pics-api call Error", e);
				});

	}

}
