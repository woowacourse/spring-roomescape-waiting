package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.AuthAdminInterceptor;
import roomescape.controller.AuthArgumentResolver;
import roomescape.service.query.MemberQueryService;
import roomescape.util.JwtTokenProvider;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final MemberQueryService memberQueryService;
    private final JwtTokenProvider jwtTokenProvider;

    public WebMvcConfiguration(MemberQueryService memberQueryService, JwtTokenProvider jwtTokenProvider) {
        this.memberQueryService = memberQueryService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthAdminInterceptor(jwtTokenProvider))
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthArgumentResolver(memberQueryService, jwtTokenProvider));
    }
}
