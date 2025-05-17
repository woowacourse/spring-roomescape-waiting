package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.config.interceptor.AdminHandlerInterceptor;
import roomescape.config.interceptor.CheckRoleHandlerInterceptor;
import roomescape.config.resolver.LoginMemberArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AdminHandlerInterceptor adminHandlerInterceptor;
    private final CheckRoleHandlerInterceptor checkRoleHandlerInterceptor;

    public WebMvcConfiguration(LoginMemberArgumentResolver loginMemberArgumentResolver,
                               AdminHandlerInterceptor adminHandlerInterceptor,
                               CheckRoleHandlerInterceptor checkRoleHandlerInterceptor) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.adminHandlerInterceptor = adminHandlerInterceptor;
        this.checkRoleHandlerInterceptor = checkRoleHandlerInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminHandlerInterceptor).addPathPatterns("/admin/**");
        registry.addInterceptor(checkRoleHandlerInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/admin/**");
    }
}
