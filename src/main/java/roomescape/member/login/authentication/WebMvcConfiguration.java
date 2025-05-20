package roomescape.member.login.authentication;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.login.authorization.LoginAuthorizationInterceptor;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private static final String ADMIN_PATH = "/admin/**";

    private final LoginAuthenticationResolver loginAuthenticationResolver;
    private final LoginAuthorizationInterceptor loginAuthorizationInterceptor;

    public WebMvcConfiguration(
            LoginAuthenticationResolver loginAuthenticationResolver,
            LoginAuthorizationInterceptor loginAuthorizationInterceptor
    ) {
        this.loginAuthenticationResolver = loginAuthenticationResolver;
        this.loginAuthorizationInterceptor = loginAuthorizationInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginAuthenticationResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginAuthorizationInterceptor).addPathPatterns(ADMIN_PATH);
    }
}
