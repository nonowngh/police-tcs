package mb.fw.tcs.common.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ModuleFieldConstants;

@Slf4j
@RestControllerAdvice
public class ModuleExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleAll(Exception e, HttpServletRequest request) {
		log.error("🚨 ModuleExceptionHandler 발생 : {}", e.getMessage());
		// 인터셉터에서 사용할 수 있도록 에러 저장
		request.setAttribute(ModuleFieldConstants.ERROR_EXCEPTION, e);
		request.setAttribute(ModuleFieldConstants.EXCEPTION_MESSAGE, e.getMessage());

		Map<String, Object> body = new HashMap<>();
		// MDC가 살아있으므로 그대로 사용 가능
		body.put(ModuleFieldConstants.TRANSACTION_ID, MDC.get(ModuleFieldConstants.TRANSACTION_ID));
		body.put(ModuleFieldConstants.STATUS, "ERROR");
		body.put(ModuleFieldConstants.MESSAGE, "처리 중 오류가 발생했습니다. [" + e.getMessage() + "]");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
