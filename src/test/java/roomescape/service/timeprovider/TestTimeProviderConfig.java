package roomescape.service.timeprovider;


import java.time.LocalDateTime;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestTimeProviderConfig {

    private LocalDateTime fixTime = LocalDateTime.of(2023, 10, 1, 12, 0);

    @Bean
    public TimeProvider timeProvider() {
        return new FixTimeProvider(this.fixTime);
    }
}
