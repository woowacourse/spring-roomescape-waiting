package roomescape;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RoomescapeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomescapeApplication.class, args);
    }

    @Bean
    public Clock systemDefaultClock() {
        return Clock.systemDefaultZone();
    }
}
