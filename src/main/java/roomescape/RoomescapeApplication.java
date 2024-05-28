package roomescape;

import org.springframework.boot.Banner;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RoomescapeApplication {
    public static void main(final String[] args) {
        SpringApplication springApplication = new SpringApplication(RoomescapeApplication.class);
        springApplication.setBannerMode(Mode.OFF);
        springApplication.run();
    }
}
