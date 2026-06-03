package roomescape.global;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ObjectProvider<LoginMemberArgumentResolver> loginMemberArgumentResolver;
    private final ObjectProvider<AdminAuthorizationInterceptor> adminAuthorizationInterceptor;

    public WebConfig(
            ObjectProvider<LoginMemberArgumentResolver> loginMemberArgumentResolver,
            ObjectProvider<AdminAuthorizationInterceptor> adminAuthorizationInterceptor
    ) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.adminAuthorizationInterceptor = adminAuthorizationInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        loginMemberArgumentResolver.ifAvailable(resolvers::add);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        adminAuthorizationInterceptor.ifAvailable(registry::addInterceptor);
    }
}
