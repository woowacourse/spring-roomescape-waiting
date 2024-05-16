package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.service.MemberService;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final MemberService memberService;

    public WebMvcConfig(final MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CheckLoginInterceptor(memberService))
                .addPathPatterns("/reservation", "/logout", "/reservation-mine");
        registry.addInterceptor(new CheckMemberInterceptor(memberService))
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/error", "/login", "/signup",
                        "/members", "/themes/popular",
                        "/css/**", "/*.ico", "/js/**", "/image/**",
                        "/reservation", "/logout", "/reservation-mine");
        registry.addInterceptor(new CheckAdminInterceptor())
                .addPathPatterns("/admin/**");
    }
}
