package mb.fw.tcs.modules.pics.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import mb.fw.tcs.common.constants.ApiPathConstants;
import mb.fw.tcs.common.constants.ModuleFieldConstants;
import mb.fw.tcs.modules.pics.api.service.PicsService;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpec;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.API_PICS_PATH)
public class PicsCommonController {

	private final PicsService picsService;

	@PostMapping("/**")
	public ResponseEntity<Object> picsCommonRequest(@RequestBody Object body, ServerHttpRequest request,
			@RequestAttribute(ModuleFieldConstants.INTERFACE_SPEC) InterfaceSpec spec) throws Exception {
		return picsService.callApi(spec, body);
	}
}
