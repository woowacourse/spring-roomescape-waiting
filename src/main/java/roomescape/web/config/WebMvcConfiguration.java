package roomescape.web.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.web.auth.CheckLoginInterceptor;
import roomescape.web.auth.MemberHandlerMethodArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private static final String ADMIN_PATH = "/admin/**";

    private final MemberHandlerMethodArgumentResolver memberHandlerMethodArgumentResolver;
    private final CheckLoginInterceptor checkLoginInterceptor;

    public WebMvcConfiguration(MemberHandlerMethodArgumentResolver memberHandlerMethodArgumentResolver,
                               CheckLoginInterceptor checkLoginInterceptor) {
        this.memberHandlerMethodArgumentResolver = memberHandlerMethodArgumentResolver;
        this.checkLoginInterceptor = checkLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkLoginInterceptor)
            .addPathPatterns(ADMIN_PATH);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberHandlerMethodArgumentResolver);
    }
}
