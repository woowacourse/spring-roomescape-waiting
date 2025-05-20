package roomescape.common.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.jwt.JwtTokenProvider;
import roomescape.common.interceptor.AdminRoleInterceptor;
import roomescape.common.resolver.LoginMemberArgumentResolver;
import roomescape.member.service.MemberService;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(memberService, jwtTokenProvider));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new AdminRoleInterceptor(memberService, jwtTokenProvider))
                .addPathPatterns("/admin/**");
    }
}