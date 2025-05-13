package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.interceptor.AuthorizationInterceptor;
import roomescape.global.interceptor.LogInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final HandlerMethodArgumentResolver handlerMethodArgumentResolver;
    private final AuthorizationInterceptor authorizationInterceptor;
    private final LogInterceptor logInterceptor;

    public WebMvcConfig(HandlerMethodArgumentResolver handlerMethodArgumentResolver,
                        AuthorizationInterceptor authorizationInterceptor, LogInterceptor logInterceptor) {
        this.handlerMethodArgumentResolver = handlerMethodArgumentResolver;
        this.authorizationInterceptor = authorizationInterceptor;
        this.logInterceptor = logInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(handlerMethodArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/h2-console", "/js/**", "/image/**", "/css/**");

        registry.addInterceptor(authorizationInterceptor)
                .order(2)
                .addPathPatterns("/**")
                .excludePathPatterns("/h2-console", "/js/**", "/image/**", "/css/**");
    }

}
