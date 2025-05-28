package roomescape.common.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.member.argumentResolver.LoginArgumentResolver;
import roomescape.admin.interceptor.AdminInterceptor;
import roomescape.common.util.JwtTokenContainer;
import roomescape.common.util.TokenCookieManager;
import roomescape.member.service.LoginService;

@Configuration
public class ReservationConfig implements WebMvcConfigurer {

    private final LoginService loginService;
    private final TokenCookieManager tokenCookieManager;
    private final JwtTokenContainer jwtTokenContainer;

    public ReservationConfig(LoginService loginService, TokenCookieManager tokenCookieManager,
                             JwtTokenContainer jwtTokenContainer) {
        this.loginService = loginService;
        this.tokenCookieManager = tokenCookieManager;
        this.jwtTokenContainer = jwtTokenContainer;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginArgumentResolver(loginService, tokenCookieManager));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminInterceptor(tokenCookieManager, jwtTokenContainer))
                .addPathPatterns("/admin/**");
    }
}
