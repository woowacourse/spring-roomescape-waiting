package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infrastructure.AuthenticatedMemberIdArgumentResolver;
import roomescape.infrastructure.CheckAdminRoleInterceptor;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final AuthenticatedMemberIdArgumentResolver authenticatedMemberIdArgumentResolver;
    private final CheckAdminRoleInterceptor checkAdminRoleInterceptor;

    public WebMvcConfiguration(
            AuthenticatedMemberIdArgumentResolver authenticatedMemberIdArgumentResolver,
            CheckAdminRoleInterceptor checkAdminRoleInterceptor
    ) {
        this.authenticatedMemberIdArgumentResolver = authenticatedMemberIdArgumentResolver;
        this.checkAdminRoleInterceptor = checkAdminRoleInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedMemberIdArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkAdminRoleInterceptor)
                .addPathPatterns("/admin/**");
    }
}
