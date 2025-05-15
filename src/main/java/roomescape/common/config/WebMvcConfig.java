package roomescape.common.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.auth.config.AuthWebConfig;

@Configuration
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Import(AuthWebConfig.class)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/reservation").setViewName("reservation");
        registry.addViewController("/reservation-mine").setViewName("reservation-mine");

        //admin
        registry.addViewController("/admin").setViewName("admin/index");
        registry.addViewController("/admin/reservation").setViewName("admin/reservation-new");
        registry.addViewController("/admin/time").setViewName("admin/time");
        registry.addViewController("/admin/theme").setViewName("admin/theme");
    }
}
