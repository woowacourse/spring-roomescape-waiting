package roomescape.auth.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.domain.AuthTokenExtractor;
import roomescape.auth.domain.AuthTokenProvider;
import roomescape.auth.ui.AuthRoleCheckInterceptor;
import roomescape.auth.ui.MemberAuthInfoArgumentResolver;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthTokenExtractor<String> authTokenExtractor;
    private final AuthTokenProvider authTokenProvider;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(
                        new AuthRoleCheckInterceptor(authTokenExtractor, authTokenProvider)
                )
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberAuthInfoArgumentResolver(authTokenExtractor, authTokenProvider));
    }
}
