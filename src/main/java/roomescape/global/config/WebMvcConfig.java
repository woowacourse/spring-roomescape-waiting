package roomescape.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.interceptor.AdminInterceptor;
import roomescape.auth.interceptor.MemberInterceptor;
import roomescape.auth.resolver.LoginUserArgumentResolver;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final AdminInterceptor adminInterceptor;
    private final MemberInterceptor memberInterceptor;

    public WebMvcConfig(LoginUserArgumentResolver loginUserArgumentResolver,
                        AdminInterceptor adminInterceptor,
                        MemberInterceptor memberInterceptor) {
        this.loginUserArgumentResolver = loginUserArgumentResolver;
        this.adminInterceptor = adminInterceptor;
        this.memberInterceptor = memberInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberInterceptor)
                .addPathPatterns("/reservation/**")
                .addPathPatterns("/reservations/**")
                .addPathPatterns("/logout");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
