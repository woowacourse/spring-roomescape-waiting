package roomescape;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import roomescape.service.TimeProvider;

@Configuration
public class TestClockConfig {

    static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 5, 7, 12, 0);

    @Bean
    @Primary
    public TimeProvider timeProvider() {
        return new TimeProvider() {
            @Override
            public LocalDate today() {
                return FIXED_NOW.toLocalDate();
            }

            @Override
            public LocalDateTime currentDateTime() {
                return FIXED_NOW;
            }
        };
    }
}