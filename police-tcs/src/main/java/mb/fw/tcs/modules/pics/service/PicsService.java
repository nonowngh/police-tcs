package mb.fw.tcs.modules.pics.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PicsService {

	@Qualifier("picsWebClient")
	private final WebClient picsWebClient;

	
}
