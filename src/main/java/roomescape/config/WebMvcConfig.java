package roomescape.config;

import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.LoginMemberArgumentResolver;
import roomescape.auth.filter.AdminFilter;
import roomescape.auth.filter.AuthFilter;
import roomescape.auth.filter.ManagerFilter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AuthFilter authFilter;
    private final AdminFilter adminFilter;
    private final ManagerFilter managerFilter;

    public WebMvcConfig(
            LoginMemberArgumentResolver loginMemberArgumentResolver,
            AuthFilter authFilter,
            AdminFilter adminFilter,
            ManagerFilter managerFilter
    ) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.authFilter = authFilter;
        this.adminFilter = adminFilter;
        this.managerFilter = managerFilter;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>(authFilter);
        registration.addUrlPatterns(
                "/reservations", "/reservations/*",
                "/waitings", "/waitings/*",
                "/payments/confirm", "/payments/fail",
                "/members/me"
        );
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilterRegistration() {
        FilterRegistrationBean<AdminFilter> registration = new FilterRegistrationBean<>(adminFilter);
        registration.addUrlPatterns("/admin", "/admin/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ManagerFilter> managerFilterRegistration() {
        FilterRegistrationBean<ManagerFilter> registration = new FilterRegistrationBean<>(managerFilter);
        registration.addUrlPatterns("/manager", "/manager/*");
        return registration;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
