package roomescape.common.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.common.auth.interceptor.AuthenticationInterceptor;
import roomescape.common.auth.interceptor.AuthorizationInterceptor;

@Configuration
@RequiredArgsConstructor
public class AuthInterceptorConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AuthorizationInterceptor authorizationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).order(1);
        registry.addInterceptor(authorizationInterceptor).order(2);
    }

}
