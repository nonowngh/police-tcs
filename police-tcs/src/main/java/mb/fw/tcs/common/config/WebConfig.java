package mb.fw.tcs.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import mb.fw.tcs.common.constants.ApiPathConstants;
import mb.fw.tcs.modules.pics.interceptor.InterfaceInterceptor;

@Configuration
@ConditionalOnProperty(prefix = "module.pics", name = "enabled", havingValue = "true", matchIfMissing = false)
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private InterfaceInterceptor interfaceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interfaceInterceptor)
                // 행공 호출하는 API에만 적용
                .addPathPatterns(ApiPathConstants.API_PICS_PATH + "/**");
                // 그 중에서도 제외하고 싶은 특정 경로가 있다면 추가
//                .excludePathPatterns("/api/external/health-check"); 
    }
}