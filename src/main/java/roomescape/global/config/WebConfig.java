package roomescape.global.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.AdminAuthorizationInterceptor;
import roomescape.global.auth.LoginMemberArgumentResolver;
import roomescape.ratelimit.RateLimitInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;
    private final ObjectProvider<RateLimitInterceptor> rateLimitInterceptorProvider;

    public WebConfig(
            LoginMemberArgumentResolver loginMemberArgumentResolver,
            AdminAuthorizationInterceptor adminAuthorizationInterceptor,
            ObjectProvider<RateLimitInterceptor> rateLimitInterceptorProvider
    ) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
        this.rateLimitInterceptorProvider = rateLimitInterceptorProvider;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthorizationInterceptor)
                .addPathPatterns("/admin/**");
        rateLimitInterceptorProvider.ifAvailable(rateLimitInterceptor ->
                registry.addInterceptor(rateLimitInterceptor)
                        .addPathPatterns("/payments/**", "/reservations/**")
        );
    }
}
