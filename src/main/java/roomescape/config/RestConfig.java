package roomescape.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.ratelimit.RateLimitInterceptor;

@Configuration
public class RestConfig implements WebMvcConfigurer {

    private final ObjectProvider<RateLimitInterceptor> rateLimitInterceptor;

    public RestConfig(ObjectProvider<RateLimitInterceptor> rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        RateLimitInterceptor interceptor = rateLimitInterceptor.getIfAvailable();
        if (interceptor == null) {
            return;
        }
        registry.addInterceptor(interceptor)
                .addPathPatterns("/payments/**", "/reservations/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "https://reservation-front-eta.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}