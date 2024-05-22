package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.service.AuthService;
import roomescape.auth.token.TokenProvider;
import roomescape.interceptor.AdminCheckInterceptor;
import roomescape.interceptor.LoginCheckInterceptor;
import roomescape.resolver.AuthenticatedMemberArgumentResolver;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthService authService;
    private final TokenProvider tokenProvider;

    public WebConfig(final AuthService authService, final TokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticatedMemberArgumentResolver(authService));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor(tokenProvider))
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**", "/js/**", "/images/**", "/fonts/**", "/*.ico",
                        "/image/default-profile.png",
                        "/", "/popular-themes", "/login", "/signup", "/members"
                );

        registry.addInterceptor(new AdminCheckInterceptor(tokenProvider))
                .order(2)
                .addPathPatterns("/admin/**");
    }
}
