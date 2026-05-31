package roomescape.integration.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
public class TestConfig {

    @Bean
    public DatabaseHelper databaseHelper(JdbcTemplate jdbcTemplate) {
        return new DatabaseHelper(jdbcTemplate);
    }

    @Bean
    public Clock clock() {
        return Clock.fixed(
                Instant.parse("2026-05-01T09:00:00+09:00"),
                ZoneId.of("Asia/Seoul")
        );
    }
}
