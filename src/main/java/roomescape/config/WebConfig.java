package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginArgumentResolver loginArgumentResolver;
    private final LoginCheckInterceptor loginCheckInterceptor;
    private final RoleCheckInterceptor roleCheckInterceptor;

    public WebConfig(
            LoginArgumentResolver loginArgumentResolver,
            LoginCheckInterceptor loginCheckInterceptor,
            RoleCheckInterceptor roleCheckInterceptor
    ) {
        this.loginArgumentResolver = loginArgumentResolver;
        this.loginCheckInterceptor = loginCheckInterceptor;
        this.roleCheckInterceptor = roleCheckInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/signup", "/members", "/login", "/logout", "/css/**", "/*.ico", "/error", "/js/**");

        registry.addInterceptor(roleCheckInterceptor)
                .order(2)
                .addPathPatterns("/admin/**");
    }
}
