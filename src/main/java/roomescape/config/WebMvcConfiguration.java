package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AdminRoleHandlerInterceptor;
import roomescape.auth.LoginMemberIdArgumentResolver;
import roomescape.auth.MemberHandlerInterceptor;
import roomescape.auth.TokenProvider;
import roomescape.service.member.MemberService;

import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final TokenProvider tokenProvider;
    private final MemberService memberService;

    public WebMvcConfiguration(TokenProvider tokenProvider, MemberService memberService) {
        this.tokenProvider = tokenProvider;
        this.memberService = memberService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MemberHandlerInterceptor(tokenProvider, memberService))
                .addPathPatterns("/reservations/**")
                .addPathPatterns("/reservation/**")
                .addPathPatterns("/logout");
        registry.addInterceptor(new AdminRoleHandlerInterceptor(tokenProvider, memberService))
                .addPathPatterns("/admin/**")
                .addPathPatterns("/members");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberIdArgumentResolver(tokenProvider));
    }
}
