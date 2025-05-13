package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.application.auth.AuthService;
import roomescape.infrastructure.AuthenticatedMemberIdArgumentResolver;
import roomescape.infrastructure.AuthenticatedMemberIdExtractor;
import roomescape.infrastructure.CheckAdminRoleInterceptor;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthService authService;
    private final AuthenticatedMemberIdExtractor authenticatedMemberIdExtractor;

    public WebMvcConfiguration(
            AuthService authService,
            AuthenticatedMemberIdExtractor authenticatedMemberIdExtractor
    ) {
        this.authService = authService;
        this.authenticatedMemberIdExtractor = authenticatedMemberIdExtractor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(
                new AuthenticatedMemberIdArgumentResolver(authenticatedMemberIdExtractor)
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                        new CheckAdminRoleInterceptor(authenticatedMemberIdExtractor, authService))
                .addPathPatterns("/admin/**");
    }
}
