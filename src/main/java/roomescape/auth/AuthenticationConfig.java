package roomescape.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AuthenticationConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    public AuthenticationConfig(LoginCheckInterceptor loginCheckInterceptor,
                                LoginMemberArgumentResolver loginMemberArgumentResolver) {
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/login/token",
                        "/api/v1/auth/logout",
                        "/api/v1/themes",
                        "/api/v1/themes/popular",
                        "/api/v1/stores",
                        "/api/v1/reservation-times",
                        "/api/v1/reservation-times/availability"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
