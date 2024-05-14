package roomescape.common;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.core.AuthenticationPrincipalArgumentResolver;
import roomescape.auth.core.AuthorizationInterceptor;
import roomescape.auth.core.AuthorizationManager;
import roomescape.auth.core.token.TokenProvider;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthorizationManager authorizationManager;
    private final TokenProvider tokenProvider;

    public WebMvcConfiguration(final AuthorizationManager authorizationManager, final TokenProvider tokenProvider) {
        this.authorizationManager = authorizationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        resolvers.add(new AuthenticationPrincipalArgumentResolver(tokenProvider, authorizationManager));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(new AuthorizationInterceptor(authorizationManager, tokenProvider))
                .addPathPatterns("/admin/**");
    }
}
