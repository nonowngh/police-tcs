package mb.fw.tcs.common.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import mb.fw.tcs.common.constants.InterfaceStatus;
import mb.fw.tcs.common.constants.ModuleConstants;

@RestControllerAdvice
public class ModuleExceptionHandler {
	
	@ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception e, HttpServletRequest request) {
        // 1. 로그에 기록할 에러 메시지를 Request에 보관
        request.setAttribute(ModuleConstants.ERROR_EXCEPTION, e);
        request.setAttribute(ModuleConstants.EXCEPTION_MESSAGE, e.getMessage());

        // 2. 클라이언트에게 보낼 응답 생성
        Map<String, Object> body = new HashMap<>();
        body.put(ModuleConstants.TRANSACTION_ID, MDC.get(ModuleConstants.TRANSACTION_ID));
        body.put(ModuleConstants.STATUS, InterfaceStatus.ERROR);
        body.put(ModuleConstants.MESSSGE, "처리 중 오류가 발생했습니다.[" + e.getMessage() +"]");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
