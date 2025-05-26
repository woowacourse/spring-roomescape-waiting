package roomescape.auth.infrastructure.config;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.infrastructure.methodargument.AuthorizationPrincipalInterceptor;
import roomescape.auth.infrastructure.methodargument.CheckMemberRoleInterceptor;
import roomescape.auth.infrastructure.methodargument.LoginMemberArgumentResolver;

@Configuration
@AllArgsConstructor
public class AuthConfig implements WebMvcConfigurer {

    private final AuthorizationPrincipalInterceptor authorizationPrincipalInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckMemberRoleInterceptor())
            .order(2)
            .addPathPatterns("/admin/**");
        registry.addInterceptor(authorizationPrincipalInterceptor)
            .order(1)
            .addPathPatterns("/**");
    }
}
