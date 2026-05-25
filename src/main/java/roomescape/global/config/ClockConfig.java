package roomescape.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ClockConfig {

    @Value("${spring.clock.zone}")
    private String clockZone;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(clockZone));
    }
}
