package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.interceptor.AuthInterceptor;
import roomescape.controller.support.AuthArgumentResolver;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final AuthArgumentResolver authArgumentResolver;
    private final AuthInterceptor authInterceptor;

    public AuthConfig(AuthArgumentResolver authArgumentResolver, AuthInterceptor authInterceptor) {
        this.authArgumentResolver = authArgumentResolver;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**");
    }
}
// 인터셉터 추가해서 수동 승인 접근 exception만 던지도록
