package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AdminAuthHandlerInterceptor;
import roomescape.auth.AuthenticatedMemberArgumentResolver;

import java.util.List;

@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    private final AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver;
    private final AdminAuthHandlerInterceptor adminAuthHandlerInterceptor;

    public AuthWebConfig(AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver,
                         AdminAuthHandlerInterceptor adminAuthHandlerInterceptor) {
        this.authenticatedMemberArgumentResolver = authenticatedMemberArgumentResolver;
        this.adminAuthHandlerInterceptor = adminAuthHandlerInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthHandlerInterceptor).addPathPatterns("/admin/**","/members");
    }
}
