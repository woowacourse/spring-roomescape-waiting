package roomescape.presentation;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.presentation.auth.CheckAdminInterceptor;
import roomescape.presentation.auth.UserArgumentResolver;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthenticationTokenHandler authenticationTokenHandler;

    public WebMvcConfiguration(final AuthenticationTokenHandler authenticationTokenHandler) {
        this.authenticationTokenHandler = authenticationTokenHandler;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserArgumentResolver(authenticationTokenHandler));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CheckAdminInterceptor(authenticationTokenHandler))
                .addPathPatterns("/admin/**");
    }
}
