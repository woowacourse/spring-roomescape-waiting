package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.interceptor.AdminCheckInterceptor;
import roomescape.auth.interceptor.MemberCheckInterceptor;
import roomescape.auth.resolver.AuthenticationPrincipalArgumentResolver;
import roomescape.auth.service.AuthService;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final AuthService authService;

    public WebMvcConfiguration(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AdminCheckInterceptor(authService))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new MemberCheckInterceptor(authService))
                .addPathPatterns("/reservations", "/login/check");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalArgumentResolver(authService));
    }
}
