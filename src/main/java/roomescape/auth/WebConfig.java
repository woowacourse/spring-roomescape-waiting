package roomescape.auth;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import roomescape.member.service.MemberService;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberService memberService;

    public WebConfig(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/reservation").setViewName("reservation");
        registry.addViewController("/time").setViewName("time");
        registry.addViewController("/theme").setViewName("theme");
        registry.addViewController("/popular").setViewName("popular");
        registry.addViewController("/my-reservations").setViewName("my-reservations");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/bookings/**", "/reservations-mine", "/waitings/**", "/member/**");
        registry.addInterceptor(new AdminInterceptor(memberService))
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(memberService));
    }

    @Bean
    public RouterFunction<ServerResponse> viewRoutes() {
        return RouterFunctions.route()
                .GET("/login", request -> ServerResponse.ok().render("login"))
                .GET("/signup", request -> ServerResponse.ok().render("signup"))
                .build();
    }
}
