package mb.fw.tcs.modules.pics.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ApiPathConstants;
import mb.fw.tcs.common.constants.ModuleFieldConstants;
import mb.fw.tcs.modules.pics.api.service.PicsService;
import mb.fw.tcs.modules.pics.common.spec.InterfaceSpec;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPathConstants.API_PICS_PATH)
public class PicsCommonController {

	private final PicsService picsService;

	@PostMapping("/**")
	public ResponseEntity<Object> picsCommonRequest(@RequestBody Object body, HttpServletRequest request,
			@RequestAttribute(ModuleFieldConstants.INTERFACE_SPEC) InterfaceSpec spec) throws Exception {

		log.info("[{}] ({}) 요청 처리...", spec.getInterfaceId(), spec.getInterfaceDescription());
		return picsService.callApi(spec, body);
	}
}
