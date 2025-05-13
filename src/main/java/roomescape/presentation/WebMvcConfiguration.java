package roomescape.presentation;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.application.AuthenticationService;
import roomescape.domain.AuthenticationTokenProvider;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthenticationService authenticationService;
    private final AuthenticationTokenProvider authenticationTokenProvider;

    public WebMvcConfiguration(final AuthenticationService authenticationService,
        final AuthenticationTokenProvider authenticationTokenProvider) {
        this.authenticationService = authenticationService;
        this.authenticationTokenProvider = authenticationTokenProvider;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserArgumentResolver(authenticationService));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CheckAdminInterceptor(authenticationTokenProvider))
            .addPathPatterns("/admin/**");
    }
}
