package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.domain.login.controller.MemberArgumentResolver;
import roomescape.global.auth.CheckAdminPermissionInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberArgumentResolver memberArgumentResolver;

    private final CheckAdminPermissionInterceptor checkAdminPermissionInterceptor;

    public WebConfig(MemberArgumentResolver memberArgumentResolver,
                     CheckAdminPermissionInterceptor checkAdminPermissionInterceptor) {
        this.memberArgumentResolver = memberArgumentResolver;
        this.checkAdminPermissionInterceptor = checkAdminPermissionInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkAdminPermissionInterceptor)
                .addPathPatterns("/admin/**");
    }
}
