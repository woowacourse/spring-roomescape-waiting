package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.login.LoginCheckInterceptor;
import roomescape.member.MemberRequestArgumentResolver;

import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final MemberRequestArgumentResolver memberRequestArgumentResolver;
    private final LoginCheckInterceptor loginCheckInterceptor;

    public WebMvcConfiguration(MemberRequestArgumentResolver memberRequestArgumentResolver,
                               LoginCheckInterceptor loginCheckInterceptor) {
        this.memberRequestArgumentResolver = memberRequestArgumentResolver;
        this.loginCheckInterceptor = loginCheckInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberRequestArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor).addPathPatterns("/admin/**");
    }
}
