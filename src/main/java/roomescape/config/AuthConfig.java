package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.interceptor.AdminAccessInterceptor;
import roomescape.controller.interceptor.AuthenticationExtractInterceptor;
import roomescape.controller.support.AuthArgumentResolver;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final AuthenticationExtractInterceptor authenticationExtractInterceptor;
    private final AuthArgumentResolver authArgumentResolver;
    private final AdminAccessInterceptor adminAccessInterceptor;

    public AuthConfig(AuthenticationExtractInterceptor authenticationExtractInterceptor,
                      AuthArgumentResolver authArgumentResolver,
                      AdminAccessInterceptor adminAccessInterceptor) {
        this.authenticationExtractInterceptor = authenticationExtractInterceptor;
        this.authArgumentResolver = authArgumentResolver;
        this.adminAccessInterceptor = adminAccessInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationExtractInterceptor)
                .excludePathPatterns("/", "/login", "/signup", "/css/**", "/js/**", "/image/**", "/themes/popular");
        registry.addInterceptor(adminAccessInterceptor)
                .addPathPatterns("/admin/**");
    }
}
