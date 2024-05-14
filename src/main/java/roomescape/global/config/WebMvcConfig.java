package roomescape.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.auth.interceptor.AdminInterceptor;
import roomescape.global.auth.resolver.MemberIdResolver;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final MemberIdResolver memberIdResolver;
    private final AdminInterceptor adminInterceptor;

    public WebMvcConfig(final MemberIdResolver memberIdResolver, final AdminInterceptor adminInterceptor) {
        this.memberIdResolver = memberIdResolver;
        this.adminInterceptor = adminInterceptor;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberIdResolver);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor);
    }
}
