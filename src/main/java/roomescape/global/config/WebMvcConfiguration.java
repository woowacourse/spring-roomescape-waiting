package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AdminHandlerInterceptor;
import roomescape.auth.AuthenticatedMemberArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver;
    private final AdminHandlerInterceptor adminHandlerInterceptor;

    public WebMvcConfiguration(AuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver,
                               AdminHandlerInterceptor adminHandlerInterceptor) {
        this.authenticatedMemberArgumentResolver = authenticatedMemberArgumentResolver;
        this.adminHandlerInterceptor = adminHandlerInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminHandlerInterceptor)
                .addPathPatterns("/admin/**");
    }
}
