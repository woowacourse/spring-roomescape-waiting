package roomescape.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.service.AuthService;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthService authService;

    public WebMvcConfig(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(authService));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new CheckLoginInterceptor(authService))
                .addPathPatterns("/reservation", "/logout", "/reservation-mine", "/admin/**");
        registry.addInterceptor(new CheckAdminInterceptor(authService))
                .addPathPatterns("/admin/**");
        registry.addInterceptor(new CheckAdminAndHttpMethodInterceptor(authService, GET))
                .addPathPatterns("/waiting", "/reservations", "/members");
        registry.addInterceptor(new CheckAdminAndHttpMethodInterceptor(authService, POST))
                .addPathPatterns("/themes", "/times");
        registry.addInterceptor(new CheckAdminAndHttpMethodInterceptor(authService, DELETE))
                .addPathPatterns("/themes/*", "/times/*");
    }
}
