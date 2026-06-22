package roomescape.ratelimit;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitWebConfig implements WebMvcConfigurer {

    private final ObjectProvider<RateLimitInterceptor> rateLimitInterceptorProvider;

    public RateLimitWebConfig(
            ObjectProvider<RateLimitInterceptor> rateLimitInterceptorProvider
    ) {
        this.rateLimitInterceptorProvider = rateLimitInterceptorProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        rateLimitInterceptorProvider.ifAvailable(interceptor ->
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/payments/**", "/reservations/**")
        );
    }
}
