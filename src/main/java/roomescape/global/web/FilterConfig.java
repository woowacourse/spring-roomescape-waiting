package roomescape.global.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AdminAccessFilter> adminAccessFilter() {
        FilterRegistrationBean<AdminAccessFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AdminAccessFilter());
        registration.addUrlPatterns("/api/admin/*");
        return registration;
    }
}