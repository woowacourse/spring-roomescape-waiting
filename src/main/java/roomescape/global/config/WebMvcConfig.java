package roomescape.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.global.exception.support.BusinessExceptionMappingJackson2HttpMessageConverter;
import roomescape.reservation.auth.ReservationOwnerInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final ReservationOwnerInterceptor interceptor;

    public WebMvcConfig(ObjectMapper objectMapper, ReservationOwnerInterceptor interceptor) {
        this.objectMapper = objectMapper;
        this.interceptor = interceptor;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addFirst( new BusinessExceptionMappingJackson2HttpMessageConverter(objectMapper));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
