package mb.fw.tcs.modules.pics.spec;

import lombok.Data;

@Data
public class InterfaceSpec {

	// 인터페이스 아이디
	private String interfaceId;
	// 인터페이스 명
	private String interfaceDescription;
	// path 매핑
	private String interfaceMappingPath;
	// 목적지 서비스 path
	private String apiPath;
	// 목적지 서비스 api-key
	private String apiKey;
	// open-api 요청 타임아웃(초)
//	private int apiRequestTimeoutSeconds = 30;
	// 인터페이스 사용 여부
	private boolean enabled = true;

}
