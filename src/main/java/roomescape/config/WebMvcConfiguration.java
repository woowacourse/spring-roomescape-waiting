package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.controller.LoginMemberArgumentResolver;
import roomescape.controller.api.dto.request.TokenContextRequest;
import roomescape.service.MemberService;
import roomescape.util.TokenProvider;

import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    private final TokenContextRequest tokenContextRequest;

    public WebMvcConfiguration(final MemberService memberService, final TokenProvider tokenProvider, final TokenContextRequest tokenContextRequest) {
        this.memberService = memberService;
        this.tokenProvider = tokenProvider;
        this.tokenContextRequest = tokenContextRequest;
    }


    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CheckAdminInterceptor(memberService, tokenProvider, tokenContextRequest))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new CheckLoginInterceptor(memberService,tokenProvider, tokenContextRequest))
                .addPathPatterns("/login/check")
                .addPathPatterns("/reservations/**");
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(tokenContextRequest));
    }
}
