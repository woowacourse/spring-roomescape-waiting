package roomescape.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.auth.AdminPageInterceptor;
import roomescape.member.auth.AuthorizationInterceptor;
import roomescape.member.auth.MemberInfoArgumentResolver;
import roomescape.member.auth.jwt.JwtTokenExtractor;
import roomescape.member.service.AuthService;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthService authService;
    private final JwtTokenExtractor jwtTokenExtractor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberInfoArgumentResolver(jwtTokenExtractor, authService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizationInterceptor(jwtTokenExtractor, authService))
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/signup");

        registry.addInterceptor(new AdminPageInterceptor(jwtTokenExtractor, authService))
                .addPathPatterns("/admin/**");
    }
}
