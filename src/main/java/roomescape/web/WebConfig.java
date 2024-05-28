package roomescape.web;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.web.argumentresolver.LoginMemberArgumentResolver;
import roomescape.web.argumentresolver.ReservationFilterArgumentResolver;
import roomescape.web.interceptor.AdminAuthorizationInterceptor;
import roomescape.web.interceptor.UserAuthorizationInterceptor;

@Configuration
class WebConfig implements WebMvcConfigurer {

    private final AdminAuthorizationInterceptor adminAuthorizationInterceptor;
    private final UserAuthorizationInterceptor userAuthorizationInterceptor;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final ReservationFilterArgumentResolver reservationFilterArgumentResolver;

    public WebConfig(AdminAuthorizationInterceptor adminAuthorizationInterceptor,
                     UserAuthorizationInterceptor userAuthorizationInterceptor,
                     LoginMemberArgumentResolver loginMemberArgumentResolver,
                     ReservationFilterArgumentResolver reservationFilterArgumentResolver) {
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
        this.userAuthorizationInterceptor = userAuthorizationInterceptor;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.reservationFilterArgumentResolver = reservationFilterArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthorizationInterceptor).addPathPatterns("/admin/**");
        registry.addInterceptor(userAuthorizationInterceptor).addPathPatterns("/reservation/**");
        registry.addInterceptor(userAuthorizationInterceptor).addPathPatterns("/reservation-mine");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
        resolvers.add(reservationFilterArgumentResolver);
    }
}
