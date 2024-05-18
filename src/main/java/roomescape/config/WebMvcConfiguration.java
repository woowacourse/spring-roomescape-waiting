package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.LoginMemberArgumentResolver;
import roomescape.service.MemberService;
import roomescape.util.TokenProvider;

import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    public WebMvcConfiguration(final MemberService memberService, final TokenProvider tokenProvider) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
    }


    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CheckLoginInterceptor(memberService, tokenProvider))
                .addPathPatterns("/login/check")
                .addPathPatterns("/reservations/**")
                .addPathPatterns("/admin/**")
                .order(1);
        registry.addInterceptor(new CheckAdminInterceptor())
                .addPathPatterns("/admin/**")
                .order(2);
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}
