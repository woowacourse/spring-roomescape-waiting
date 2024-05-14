package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infrastructure.CheckAuthenticationInterceptor;
import roomescape.infrastructure.LoginMemberArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final CheckAuthenticationInterceptor checkAuthenticationInterceptor;

    public WebMvcConfiguration(final LoginMemberArgumentResolver loginMemberArgumentResolver,
                               final CheckAuthenticationInterceptor checkAuthenticationInterceptor) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.checkAuthenticationInterceptor = checkAuthenticationInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkAuthenticationInterceptor)
                .addPathPatterns("/admin/**");
    }
}
