package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.config.handler.AdminAuthorizationInterceptor;
import roomescape.config.handler.AuthenticationArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final AuthenticationArgumentResolver authenticationArgumentResolver;
    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;

    public WebMvcConfiguration(AuthenticationArgumentResolver authenticationArgumentResolver,
                               AdminAuthorizationInterceptor adminAuthorizationInterceptor) {
        this.authenticationArgumentResolver = authenticationArgumentResolver;
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticationArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthorizationInterceptor)
                .addPathPatterns("/admin/**");
    }
}
