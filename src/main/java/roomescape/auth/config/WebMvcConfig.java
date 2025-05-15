package roomescape.auth.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import roomescape.auth.controller.AdminInterceptor;
import roomescape.auth.controller.LoginMemberArgumentResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AdminInterceptor adminInterceptor;

    public WebMvcConfig(final LoginMemberArgumentResolver loginMemberArgumentResolver, final AdminInterceptor adminInterceptor) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor);
    }
}
