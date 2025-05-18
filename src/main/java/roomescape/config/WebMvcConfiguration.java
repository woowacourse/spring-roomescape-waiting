package roomescape.config;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.JwtProvider;

@Configuration
@AllArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final JwtProvider jwtProvider;

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalArgumentResolver(jwtProvider));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizationAdminInterceptor(jwtProvider))
                .addPathPatterns("/admin/**");
    }
}
