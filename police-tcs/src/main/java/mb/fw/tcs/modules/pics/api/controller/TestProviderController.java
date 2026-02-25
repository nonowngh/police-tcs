package mb.fw.tcs.modules.pics.api.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class TestProviderController {

	@PostMapping("/piss/**") // 모든 경로 수용
	public ResponseEntity<Map<String, Object>> echo(HttpServletRequest request, @RequestHeader HttpHeaders headers,
			@RequestBody(required = false) String body) {

		log.info("========== [Test Provider Received] ==========");
		log.info("Method: {}", request.getMethod());
		log.info("URI: {}", request.getRequestURI());
		log.info("Headers: {}", headers);
		log.info("Body: {}", body);
		log.info("==============================================");

		// 응답 데이터 구성
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("receivedBody", body);
		response.put("receivedHeaders", headers);

		return ResponseEntity.ok(response);
	}
}
