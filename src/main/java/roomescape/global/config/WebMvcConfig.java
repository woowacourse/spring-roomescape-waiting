package roomescape.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.reservation.auth.ReservationOwnerInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ReservationOwnerInterceptor reservationOwnerInterceptor;

    public WebMvcConfig(ReservationOwnerInterceptor interceptor) {
        this.reservationOwnerInterceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reservationOwnerInterceptor);
    }
}
