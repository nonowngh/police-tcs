package mb.fw.tcs.modules.pics.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import mb.fw.tcs.common.constants.ApiPathConstants;
import mb.fw.tcs.modules.pics.api.interceptor.InterfaceInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private InterfaceInterceptor interfaceInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interfaceInterceptor)
				// 행공 호출하는 API에만 적용
				.addPathPatterns(ApiPathConstants.API_PICS_PATH + "/**");
	}
}