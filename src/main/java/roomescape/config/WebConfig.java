package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.ratelimit.RateLimitInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/reservations", "/payments/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/reservation").setViewName("reservation");
        registry.addViewController("/my").setViewName("my-reservation");
        registry.addViewController("/popular").setViewName("popular");
        registry.addViewController("/payment/success").setViewName("payment/success");
        registry.addViewController("/payment/fail").setViewName("payment/fail");
        registry.addViewController("/admin").setViewName("admin/index");
        registry.addViewController("/admin/").setViewName("admin/index");
        registry.addViewController("/admin/theme").setViewName("admin/theme");
        registry.addViewController("/admin/time").setViewName("admin/time");
    }
}
