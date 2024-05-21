package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.filter.CheckAdminRoleInterceptor;
import roomescape.filter.MemberArgumentResolver;

import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final MemberArgumentResolver memberArgumentResolver;
    private final CheckAdminRoleInterceptor checkAdminRoleInterceptor;

    public WebMvcConfiguration(final MemberArgumentResolver memberArgumentResolver, final CheckAdminRoleInterceptor checkAdminRoleInterceptor) {
        this.memberArgumentResolver = memberArgumentResolver;
        this.checkAdminRoleInterceptor = checkAdminRoleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkAdminRoleInterceptor).addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberArgumentResolver);
    }
}
