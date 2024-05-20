package roomescape.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateTimeConfiguration {

    @Bean
    public DateTimeFormatter getNowDateTimeFormatter() {
        return new NowDateTimeFormatter();
    }
}
