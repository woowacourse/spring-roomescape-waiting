package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final HandlerMethodArgumentResolver argumentResolver;
    private final HandlerInterceptor checkRoleInterceptor;
    private final HandlerInterceptor checkUserInterceptor;

    public WebMvcConfiguration(
        HandlerMethodArgumentResolver argumentResolver,
        HandlerInterceptor checkRoleInterceptor,
        HandlerInterceptor checkUserInterceptor) {

        this.argumentResolver = argumentResolver;
        this.checkRoleInterceptor = checkRoleInterceptor;
        this.checkUserInterceptor = checkUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkRoleInterceptor)
            .addPathPatterns("/admin/**");

        registry.addInterceptor(checkUserInterceptor)
            .addPathPatterns("/reservation")
            .addPathPatterns("/reservations");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(argumentResolver);
    }
}
