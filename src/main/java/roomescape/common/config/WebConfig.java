package roomescape.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/reservation").setViewName("forward:/reservations.html");
        registry.addViewController("/my-reservations").setViewName("forward:/my-reservations.html");
        registry.addViewController("/payment-success").setViewName("forward:/payment-success.html");
        registry.addViewController("/payment-fail").setViewName("forward:/payment-fail.html");
        registry.addViewController("/admin").setViewName("forward:/admin.html");
    }
}
