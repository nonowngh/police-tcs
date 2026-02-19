package mb.fw.tcs.modules.pics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mb.fw.tcs.common.constants.ApiPathConstants;
import mb.fw.tcs.common.constants.ModuleConstants;
import mb.fw.tcs.modules.pics.service.PicsService;
import mb.fw.tcs.modules.pics.spec.InterfaceSpec;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.API_PICS_PATH)
public class PicsCommonController {

	private final PicsService picsService;

	@PostMapping("/**")
	public Mono<ResponseEntity<Object>> picsCommonRequest(@RequestBody Object body, ServerHttpRequest request,
			@RequestAttribute(ModuleConstants.INTERFACE_SPEC) InterfaceSpec spec) {
		return picsService.callApig(spec, body);
	}
}
