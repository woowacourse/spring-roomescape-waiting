package roomescape.infrastructure.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infrastructure.authentication.AdminAuthenticationInterceptor;
import roomescape.infrastructure.authentication.LoginMemberArgumentResolver;
import roomescape.infrastructure.authentication.MemberAuthenticationInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AdminAuthenticationInterceptor adminAuthenticationInterceptor;
    private final MemberAuthenticationInterceptor memberAuthenticationInterceptor;

    public WebMvcConfig(LoginMemberArgumentResolver loginMemberArgumentResolver,
                        AdminAuthenticationInterceptor adminAuthenticationInterceptor,
                        MemberAuthenticationInterceptor memberAuthenticationInterceptor) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.adminAuthenticationInterceptor = adminAuthenticationInterceptor;
        this.memberAuthenticationInterceptor = memberAuthenticationInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthenticationInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/", "/login/**", "logout", "/themes/popular")
                .excludePathPatterns("/css/**", "/js/**", "/image/**");

        registry.addInterceptor(memberAuthenticationInterceptor)
                .addPathPatterns("/reservations/**", "/themes/**", "/times/**")
                .excludePathPatterns("/", "/login/**", "/logout", "/themes/popular")
                .excludePathPatterns("/css/**", "/js/**", "/image/**");
    }
}
