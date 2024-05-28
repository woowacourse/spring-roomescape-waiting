package roomescape.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminCheckInterceptor adminCheckInterceptor;
    private final LoginArgumentResolver loginArgumentResolver;

    public WebConfig(
            LoginCheckInterceptor loginCheckInterceptor,
            AdminCheckInterceptor adminCheckInterceptor,
            LoginArgumentResolver loginArgumentResolver
    ) {
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.loginArgumentResolver = loginArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/themes", "/themes/**", "/times/available")
                .excludePathPatterns("/**/*.html", "/**/*.css", "/**/*.js");
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns("/admin", "/admin/**");
    }
}
