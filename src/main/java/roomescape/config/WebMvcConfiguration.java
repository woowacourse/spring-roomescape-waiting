package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.AuthAdminInterceptor;
import roomescape.controller.AuthArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthArgumentResolver authArgumentResolver;
    private final AuthAdminInterceptor authAdminInterceptor;

    public WebMvcConfiguration(AuthArgumentResolver authArgumentResolver, AuthAdminInterceptor authAdminInterceptor) {
        this.authArgumentResolver = authArgumentResolver;
        this.authAdminInterceptor = authAdminInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authAdminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }
}
