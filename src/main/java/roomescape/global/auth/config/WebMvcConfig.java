package roomescape.global.auth.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.infrastructure.AuthorizationExtractor;
import roomescape.global.auth.infrastructure.JwtProvider;
import roomescape.global.auth.interceptor.AdminAuthInterceptor;
import roomescape.global.auth.resolver.AuthenticationPrincipalArgumentResolver;
import roomescape.global.auth.service.AuthService;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthService authService;
    private final AuthorizationExtractor authorizationExtractor;
    private final JwtProvider jwtProvider;

    public WebMvcConfig(final AuthService authService, final AuthorizationExtractor authorizationExtractor,
                        final JwtProvider jwtProvider) {
        this.authService = authService;
        this.authorizationExtractor = authorizationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalArgumentResolver(authService, authorizationExtractor));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor(authorizationExtractor, jwtProvider));
    }
}
