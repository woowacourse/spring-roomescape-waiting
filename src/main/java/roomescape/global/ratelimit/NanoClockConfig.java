package roomescape.global.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NanoClockConfig {

    @Bean
    public NanoClock nanoClock() {
        return System::nanoTime;
    }
}
