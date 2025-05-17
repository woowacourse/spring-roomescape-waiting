package roomescape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import roomescape.infrastructure.auth.config.WebMvcConfiguration;

@SpringBootApplication
@Import(WebMvcConfiguration.class)
public class RoomescapeApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoomescapeApplication.class, args);
    }
}
