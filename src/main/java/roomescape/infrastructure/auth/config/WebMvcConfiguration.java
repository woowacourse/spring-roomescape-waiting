package roomescape.infrastructure.auth.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infrastructure.auth.intercept.AdminRequestMatcher;
import roomescape.infrastructure.auth.intercept.AuthenticationPrincipalResolver;
import roomescape.infrastructure.auth.intercept.PrevRequestHandler;
import roomescape.infrastructure.auth.intercept.RequestMatcher;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;

@Configuration
@Import(AuthenticationConfig.class)
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    public WebMvcConfiguration(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalResolver(jwtTokenProvider));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PrevRequestHandler(jwtTokenProvider, requestMatchers()));
    }

    @Bean
    public List<RequestMatcher> requestMatchers() {
        return List.of(new AdminRequestMatcher());
    }
}
