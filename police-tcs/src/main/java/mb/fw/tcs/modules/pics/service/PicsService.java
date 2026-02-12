package mb.fw.tcs.modules.pics.service;

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
import mb.fw.tcs.common.constants.ModuleConstants;
import mb.fw.tcs.modules.pics.spec.InterfaceSpec;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PicsService {

	@Qualifier("picsWebClient")
	private final WebClient picsWebClient;

	private final PicsApiConfig picsApiConfig;

	public Mono<ResponseEntity<Object>> callApi(InterfaceSpec spec, Object requestBody) {

		String picsTransactionId = MDC.get(ModuleConstants.PICS_TRANSACTION_ID);
		String apikey = spec.getApiKey();
		String myCertServerId = picsApiConfig.getMyCertServerId();
		String path = spec.getApiPath();

		return picsWebClient.post().uri(uriBuilder -> uriBuilder.path(path).build())
				.header(HttpHeaders.HOST.toUpperCase(), "")
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header("api_key", apikey)
				.header("cert_server_id", myCertServerId)
				.header(ModuleConstants.PICS_TRANSACTION_ID, picsTransactionId)
				.header("hpki_yn", "Y")
				.bodyValue(requestBody)
				.retrieve().toEntity(Object.class)
				.doOnNext(res -> log.info("[pics-api-success] tx-id: {}, status: {}", picsTransactionId, res.getStatusCode()))
				.onErrorMap(e -> {
					log.error("[pics-api-error] tx-id: {}, message: {}", picsTransactionId, e.getMessage());
					return new RuntimeException("pics-api call Error", e);
				});

	}

}
