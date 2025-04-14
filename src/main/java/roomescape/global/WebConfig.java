package roomescape.global;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.resolver.LoginMemberArgumentResolver;
import roomescape.member.service.AutoService;
import roomescape.reservation.resolver.AdminAuthorizationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final AutoService autoService;

    public WebConfig(LoginMemberArgumentResolver loginMemberArgumentResolver, AutoService autoService) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.autoService = autoService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthorizationInterceptor(autoService))
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

}
