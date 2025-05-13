package roomescape.common.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.common.interceptor.AdminRoleInterceptor;
import roomescape.common.resolver.LoginMemberArgumentResolver;
import roomescape.member.service.AuthService;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final AuthService authService;

    public WebConfiguration(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(authService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminRoleInterceptor(authService))
                .addPathPatterns("/admin/**");
    }
}
