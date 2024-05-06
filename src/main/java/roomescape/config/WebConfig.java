package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.application.auth.TokenManager;
import roomescape.presentation.auth.AdminRoleInterceptor;
import roomescape.presentation.auth.CredentialContext;
import roomescape.presentation.auth.LoginMemberIdArgumentResolver;
import roomescape.presentation.auth.PermissionCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final TokenManager tokenManager;
    private final CredentialContext context;

    public WebConfig(TokenManager tokenManager, CredentialContext context) {
        this.tokenManager = tokenManager;
        this.context = context;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminRoleInterceptor(tokenManager, context))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new PermissionCheckInterceptor(tokenManager, context));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberIdArgumentResolver(tokenManager, context));
    }
}
