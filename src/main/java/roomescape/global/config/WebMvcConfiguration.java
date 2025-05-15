package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.JwtTokenProvider;
import roomescape.global.config.interceptor.CheckAdminInterceptor;
import roomescape.global.config.resolver.LoginMemberArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final JwtTokenProvider jwtTokenProvider;

    public WebMvcConfiguration(LoginMemberArgumentResolver loginMemberArgumentResolver,
                               JwtTokenProvider jwtTokenProvider) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckAdminInterceptor(jwtTokenProvider))
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
