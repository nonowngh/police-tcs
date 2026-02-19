package mb.fw.tcs.modules.pics.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ModuleConstants;
import mb.fw.tcs.common.utils.PicsTransactionIdGenerator;
import mb.fw.tcs.modules.pics.spec.InterfaceSpec;
import mb.fw.tcs.modules.pics.spec.InterfaceSpecLoader;

@Slf4j
@Component
public class InterfaceInterceptor implements HandlerInterceptor {

	private final InterfaceSpecLoader specLoader;

	public InterfaceInterceptor(InterfaceSpecLoader specLoader) {
		this.specLoader = specLoader;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String path = request.getRequestURI();

		// 1. InterfaceSpec 찾기 (없으면 예외 발생 - 이전 단계 로직)
		InterfaceSpec spec = specLoader.findSpec(path).orElseThrow(() -> new RuntimeException(
				"Interface Spec Not Found: " + path + " check interface-spec.json('interfaceMappingPath')"));

		// 2. Transaction ID 생성 및 MDC 세팅
		String interfaceId = spec.getInterfaceId();
		String picsTransactionId = PicsTransactionIdGenerator.generate();
		String transactionId = interfaceId + "_" + picsTransactionId;
		MDC.put(ModuleConstants.INTERFACE_ID, interfaceId);
		MDC.put(ModuleConstants.TRANSACTION_ID, transactionId);
		MDC.put(ModuleConstants.PICS_HEADER_TRANSACTION_ID, picsTransactionId);

		// 3. 시작 시간 기록 (실행 시간 계산용)
		request.setAttribute(ModuleConstants.START_TIME, System.currentTimeMillis());
		request.setAttribute(ModuleConstants.INTERFACE_SPEC, spec);

		// 4. 시작 로깅
		log.info("[START] [{}] Path: {}", spec.getInterfaceId(), path);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		try {
			// 1. HandlerExceptionResolver가 에러를 처리했다면 ex는 null일 수 있음
			// 그래서 request에 담아둔 에러 정보를 확인
			Exception error = (Exception) request.getAttribute(ModuleConstants.ERROR_EXCEPTION);
			String errorMsg = (String) request.getAttribute(ModuleConstants.EXCEPTION_MESSAGE);

			long startTime = (long) request.getAttribute(ModuleConstants.START_TIME);
			long duration = System.currentTimeMillis() - startTime;
			String ifId = MDC.get(ModuleConstants.INTERFACE_ID);

			if (error != null || response.getStatus() >= 400) {
				// [실패 로깅]
				log.error("[END] [{}] Result: FAIL | Status: {} | Duration: {}ms | Error: {}", ifId,
						response.getStatus(), duration, errorMsg != null ? errorMsg : "Unknown Error");
			} else {
				// [성공 로깅]
				log.info("[END] [{}] Result: SUCCESS | Status: {} | Duration: {}ms", ifId, response.getStatus(),
						duration);
			}
		} finally {
			MDC.clear(); 
		}
	}
}