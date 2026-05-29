package roomescape.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.service.MemberService;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberService memberService;

    public WebConfig(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/reservations/**", "/waitings/**", "/my-reservations")
                .excludePathPatterns("/reservations/id");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(memberService));
    }
}
