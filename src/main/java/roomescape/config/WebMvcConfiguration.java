package roomescape.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.config.interceptor.CheckAdminInterceptor;
import roomescape.config.resolver.LoginMemberArgumentResolver;
import roomescape.utility.CookieUtility;
import roomescape.utility.JwtTokenProvider;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final CookieUtility cookieUtility;
    private final JwtTokenProvider jwtTokenProvider;

    public WebMvcConfiguration(
            CookieUtility cookieUtility,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.cookieUtility = cookieUtility;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckAdminInterceptor(cookieUtility, jwtTokenProvider))
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(cookieUtility, jwtTokenProvider));
    }
}
