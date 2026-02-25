package mb.fw.tcs.modules.pics.api.interceptor;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.InterfaceStatus;
import mb.fw.tcs.common.constants.ModuleFieldConstants;
import mb.fw.tcs.common.logging.InterfaceLogging;
import mb.fw.tcs.common.utils.PicsTransactionIdGenerator;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpec;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpecLoader;

@Slf4j
@Component
public class InterfaceInterceptor implements HandlerInterceptor {

	private final InterfaceSpecLoader specLoader;

	private final Optional<InterfaceLogging> interfaceLogging;

	public InterfaceInterceptor(InterfaceSpecLoader specLoader, Optional<InterfaceLogging> interfaceLogging) {
		this.specLoader = specLoader;
		this.interfaceLogging = interfaceLogging;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String globalTraceId = request.getHeader("X-Global-Trace-ID");

		String path = request.getRequestURI();

		String interfaceId = path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path;

		InterfaceSpec spec = specLoader.findSpec(interfaceId)
				.orElseThrow(() -> new RuntimeException("Interface Spec Not Found: " + interfaceId));

		String picsTransactionId = PicsTransactionIdGenerator.generate();
		String transactionId = interfaceId + "_" + picsTransactionId;

		MDC.put(ModuleFieldConstants.INTERFACE_ID, interfaceId);
		MDC.put(ModuleFieldConstants.TRANSACTION_ID, transactionId);
		MDC.put(ModuleFieldConstants.PICS_HEADER_TRANSACTION_ID, picsTransactionId);

		request.setAttribute(ModuleFieldConstants.START_TIME, System.currentTimeMillis());
		request.setAttribute(ModuleFieldConstants.INTERFACE_SPEC, spec);

		log.info("🎬 [START - {}] Path: {}, global-trace-id: {}", transactionId, path, globalTraceId);

		interfaceLogging.ifPresent(logging -> {
			logging.asyncStartLogging(interfaceId, transactionId, spec.getSendSystemCode(), spec.getReceiveSystemCode(),
					1);
		});

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		try {
			long startTime = (long) request.getAttribute(ModuleFieldConstants.START_TIME);
			double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
			String transactionId = MDC.get(ModuleFieldConstants.TRANSACTION_ID);
			String interfaceId = MDC.get(ModuleFieldConstants.INTERFACE_ID);

			if (response.getStatus() >= 400) {
				log.error("🏁 [END - {}] Result: FAIL | Status: {} | Duration: {}ms", transactionId, response.getStatus(),
						durationSeconds);
				interfaceLogging.ifPresent(logging -> {
					logging.asyncEndLogging(interfaceId, transactionId, 1, InterfaceStatus.ERROR,
							request.getAttribute(ModuleFieldConstants.EXCEPTION_MESSAGE).toString());
				});
			} else {
				log.info("🏁 [END - {}] Result: SUCCESS | Status: {} | Duration: {}ms", transactionId, response.getStatus(),
						durationSeconds);
				interfaceLogging.ifPresent(logging -> {
					logging.asyncEndLogging(interfaceId, transactionId, 0, InterfaceStatus.SUCCESS,
							InterfaceStatus.SUCCESS.name());
				});
			}

		} finally {
			MDC.clear(); // 중요: 쓰레드 로컬 메모리 누수 방지
		}
	}
}