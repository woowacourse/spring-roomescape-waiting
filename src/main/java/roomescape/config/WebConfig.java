package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.security.service.MemberAuthService;
import roomescape.member.service.MemberService;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberAuthService memberAuthService;
    private final MemberService memberService;

    public WebConfig(MemberAuthService memberAuthService, MemberService memberService) {
        this.memberAuthService = memberAuthService;
        this.memberService = memberService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberArgumentResolver(memberAuthService));
        resolvers.add(new ReservationArgumentResolver(memberAuthService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckRoleInterceptor(memberAuthService, memberService))
                .addPathPatterns("/admin/**");
    }

}
