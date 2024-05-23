package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final CheckAdminInterceptor checkAdminInterceptor;
    private final MemberLoginInterceptor memberLoginInterceptor;

    public WebMvcConfig(LoginUserArgumentResolver loginUserArgumentResolver,
                        CheckAdminInterceptor checkAdminInterceptor,
                        final MemberLoginInterceptor memberLoginInterceptor) {
        this.loginUserArgumentResolver = loginUserArgumentResolver;
        this.checkAdminInterceptor = checkAdminInterceptor;
        this.memberLoginInterceptor = memberLoginInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkAdminInterceptor)
                .addPathPatterns("/admin/**");
        registry.addInterceptor(memberLoginInterceptor)
                .addPathPatterns("/reservation");
        registry.addInterceptor(memberLoginInterceptor)
                .addPathPatterns("/reservation-mine");
    }
}
