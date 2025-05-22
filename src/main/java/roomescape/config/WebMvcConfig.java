package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.service.MemberService;
import roomescape.jwt.TokenProvider;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    public WebMvcConfig(MemberService memberService, TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(tokenProvider, memberService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(tokenProvider, memberService)).addPathPatterns("/admin/**");
    }
}
