package roomescape.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final HandlerMethodArgumentResolver loginMemberArgumentResolver;
    private final HandlerInterceptor roleCheckHandlerInterceptor;

    public WebMvcConfig(
            HandlerMethodArgumentResolver loginMemberArgumentResolver,
            HandlerInterceptor roleCheckHandlerInterceptor
    ) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.roleCheckHandlerInterceptor = roleCheckHandlerInterceptor;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/reservation")
                .setViewName("reservation");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleCheckHandlerInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
