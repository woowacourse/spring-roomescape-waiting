package roomescape.auth.infrastructure.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.infrastructure.methodargument.AuthorizationPrincipalInterceptor;
import roomescape.auth.infrastructure.methodargument.CheckMemberRoleInterceptor;
import roomescape.auth.infrastructure.methodargument.LoginMemberArgumentResolver;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    private final CheckMemberRoleInterceptor checkMemberRoleInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AuthorizationPrincipalInterceptor authorizationPrincipalInterceptor;

    public AuthConfig(
        CheckMemberRoleInterceptor checkMemberRoleInterceptor,
        LoginMemberArgumentResolver loginMemberArgumentResolver,
        AuthorizationPrincipalInterceptor authorizationPrincipalInterceptor
    ) {
        this.checkMemberRoleInterceptor = checkMemberRoleInterceptor;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.authorizationPrincipalInterceptor = authorizationPrincipalInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkMemberRoleInterceptor)
            .order(2)
            .addPathPatterns("/admin/**");
        registry.addInterceptor(authorizationPrincipalInterceptor)
            .order(1)
            .addPathPatterns("/**");
    }
}
