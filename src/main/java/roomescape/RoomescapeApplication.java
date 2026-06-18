package roomescape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class RoomescapeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomescapeApplication.class, args);
    }
}
