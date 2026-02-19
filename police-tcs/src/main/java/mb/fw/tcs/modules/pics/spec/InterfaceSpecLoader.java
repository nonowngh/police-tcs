package mb.fw.tcs.modules.pics.spec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import mb.fw.tcs.common.constants.ModuleConstants;

@Slf4j
@Component
public class InterfaceSpecLoader {
	// Key: Path, Value: InterfaceSpec 객체 전체
	private Map<String, InterfaceSpec> specMap = new HashMap<>();

	@PostConstruct
	public void init() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<InterfaceSpec> allSpecs = mapper.readValue(
				new ClassPathResource(ModuleConstants.PICS_INTERFACE_SPEC_JSON_FILE).getInputStream(),
				new TypeReference<List<InterfaceSpec>>() {
				});

		this.specMap = allSpecs.stream().filter(InterfaceSpec::isEnabled)
				.collect(Collectors.toMap(InterfaceSpec::getInterfaceMappingPath, spec -> spec, // 객체 자신을 Value로 저장
						(existing, replacement) -> existing));

		log.info("🔍 [PICS] Interface Specification Loaded");
		specMap.forEach((path, spec) -> log
				.info("  ✅ [" + spec.getInterfaceId() + "] Path: " + path + " -> " + spec.getApiPath()));
	}

	public Optional<InterfaceSpec> findSpec(String path) {
		return Optional.ofNullable(specMap.get(path));
	}
}