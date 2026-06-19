package roomescape.infrastructure;

import java.util.List;
import roomescape.infrastructure.ratelimiter.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;
    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(
            LoginCheckInterceptor loginCheckInterceptor,
            AdminAuthorizationInterceptor adminAuthorizationInterceptor,
            LoginUserArgumentResolver loginUserArgumentResolver,
            RateLimitInterceptor rateLimitInterceptor
    ) {
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
        this.loginUserArgumentResolver = loginUserArgumentResolver;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/reservations", "/reservations/**", "/payments", "/payments/**");
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/reservations", "/reservations/**");
        registry.addInterceptor(adminAuthorizationInterceptor)
                .addPathPatterns("/admin/reservations", "/admin/reservations/**",
                        "/admin/themes", "/admin/themes/**",
                        "/admin/times", "/admin/times/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
