package roomescape.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

@TestConfiguration
public class FixedClockConfig {
    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(
                LocalDate.of(2026, 5, 5).atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
    }
}
