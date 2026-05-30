package roomescape.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.AuthInterceptor;
import roomescape.auth.OwnerOnlyArgumentResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final OwnerOnlyArgumentResolver ownerOnlyArgumentResolver;

    public WebMvcConfig(AuthInterceptor authInterceptor, OwnerOnlyArgumentResolver ownerOnlyArgumentResolver) {
        this.authInterceptor = authInterceptor;
        this.ownerOnlyArgumentResolver = ownerOnlyArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(ownerOnlyArgumentResolver);
    }
}
