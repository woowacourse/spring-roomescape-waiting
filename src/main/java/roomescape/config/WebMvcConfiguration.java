package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.login.LoginCheckInterceptor;
import roomescape.login.service.LoginService;
import roomescape.member.MemberNameResponseArgumentResolver;
import roomescape.member.MemberRequestArgumentResolver;
import roomescape.member.service.MemberService;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final MemberService memberService;
    private final LoginService loginService;

    public WebMvcConfiguration(MemberService memberService, LoginService loginService) {
        this.memberService = memberService;
        this.loginService = loginService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberNameResponseArgumentResolver(memberService));
        resolvers.add(new MemberRequestArgumentResolver(loginService));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor(loginService)).addPathPatterns("/admin/**");
    }
}
