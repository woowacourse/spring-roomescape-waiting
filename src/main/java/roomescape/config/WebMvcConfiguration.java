package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthInfoArgumentResolver authInfoArgumentResolver;
    private final CheckAdminInterceptor checkAdminInterceptor;
    private final CheckLoginInterceptor checkLoginInterceptor;

    public WebMvcConfiguration(AuthInfoArgumentResolver authInfoArgumentResolver,
                               CheckAdminInterceptor checkAdminInterceptor,
                               CheckLoginInterceptor checkLoginInterceptor) {
        this.authInfoArgumentResolver = authInfoArgumentResolver;
        this.checkAdminInterceptor = checkAdminInterceptor;
        this.checkLoginInterceptor = checkLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkLoginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/themes/ranking", "/login")
                .excludePathPatterns("/css/**", "/js/**", "/image/**", "/favicon.ico");

        registry.addInterceptor(checkAdminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authInfoArgumentResolver);
    }
}
