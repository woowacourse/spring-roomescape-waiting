package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infra.AdminCheckInterceptor;
import roomescape.infra.AuthArgumentResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthArgumentResolver authArgumentResolver;
    private final AdminCheckInterceptor interceptor;

    public WebConfig(AuthArgumentResolver authArgumentResolver, AdminCheckInterceptor interceptor) {
        this.authArgumentResolver = authArgumentResolver;
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }
}
