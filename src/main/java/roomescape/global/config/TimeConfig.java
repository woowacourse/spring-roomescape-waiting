package roomescape.global.config;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    private static final String KOREAN_TIME_ZONE_KEY = "Asia/Seoul";

    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(
                TimeZone.getTimeZone(KOREAN_TIME_ZONE_KEY)
        );
    }
}
