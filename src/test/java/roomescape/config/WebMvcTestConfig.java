package roomescape.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.web.resolver.TestAuthenticatedMemberArgumentResolver;

@TestConfiguration
@RequiredArgsConstructor
public class WebMvcTestConfig implements WebMvcConfigurer {

    private final TestAuthenticatedMemberArgumentResolver authenticatedMemberArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedMemberArgumentResolver);
    }
}
