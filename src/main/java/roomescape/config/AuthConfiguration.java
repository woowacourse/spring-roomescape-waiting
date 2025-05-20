package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.config.interceptor.CheckLoginInterceptor;
import roomescape.config.resolver.AuthArgumentResolver;
import roomescape.service.AuthService;

@Configuration
public class AuthConfiguration implements WebMvcConfigurer {

    private final AuthService authService;

    public AuthConfiguration(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthArgumentResolver(authService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckLoginInterceptor(authService))
            .addPathPatterns("/admin/**");
    }
}
