package mb.fw.tcs.modules.pics.api.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ModuleFieldConstants;
import mb.fw.tcs.common.utils.PicsTransactionIdGenerator;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpec;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpecLoader;

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

        log.info("[START] [{}] Path: {}", transactionId, path);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            long startTime = (long) request.getAttribute(ModuleFieldConstants.START_TIME);
            long duration = System.currentTimeMillis() - startTime;
            String transactionId = MDC.get(ModuleFieldConstants.TRANSACTION_ID);

            if (response.getStatus() >= 400) {
                log.error("[END] [{}] Result: FAIL | Status: {} | Duration: {}ms", transactionId, response.getStatus(), duration);
            } else {
                log.info("[END] [{}] Result: SUCCESS | Status: {} | Duration: {}ms", transactionId, response.getStatus(), duration);
            }
        } finally {
            MDC.clear(); // 중요: 쓰레드 로컬 메모리 누수 방지
        }
    }
}