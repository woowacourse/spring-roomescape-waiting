package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infra.AdminCheckInterceptor;
import roomescape.infra.AuthArgumentResolver;
import roomescape.infra.LoginCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthArgumentResolver authArgumentResolver;
    private final AdminCheckInterceptor adminCheckInterceptor;
    private final LoginCheckInterceptor loginCheckInterceptor;

    public WebConfig(
            AuthArgumentResolver authArgumentResolver,
            AdminCheckInterceptor adminCheckInterceptor,
            LoginCheckInterceptor loginCheckInterceptor
    ) {
        this.authArgumentResolver = authArgumentResolver;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.loginCheckInterceptor = loginCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns("/admin/**");

        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login/**", "/", "/themes/ranking");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authArgumentResolver);
    }
}
