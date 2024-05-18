package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.helper.AdminCheckInterceptor;
import roomescape.controller.helper.LoginMemberArgumentResolver;

@Configuration
public class AuthWebMvcConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AdminCheckInterceptor adminCheckInterceptor;

    public AuthWebMvcConfig(LoginMemberArgumentResolver loginMemberArgumentResolver,
                            AdminCheckInterceptor adminCheckInterceptor) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.adminCheckInterceptor = adminCheckInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/error", "/css/**");
    }
}
