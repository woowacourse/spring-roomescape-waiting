package roomescape.support;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(ServiceIntegrationTest.ServiceIntegrationTestConfig.class)
public abstract class ServiceIntegrationTest {

    @Autowired
    DatabaseHelper databaseHelper;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
    }

    @TestConfiguration
    static class ServiceIntegrationTestConfig {

        @Bean
        public Clock clock() {
            return Clock.fixed(
                    Instant.parse("2026-05-01T09:00:00+09:00"),
                    ZoneId.of("Asia/Seoul")
            );
        }

        @Bean
        public DatabaseHelper databaseHelper(JdbcTemplate jdbcTemplate) {
            return new DatabaseHelper(jdbcTemplate);
        }
    }
}
