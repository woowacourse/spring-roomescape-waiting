package roomescape.e2e;

import io.restassured.RestAssured;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.e2e.E2ETest.WebConfig;
import roomescape.support.DatabaseHelper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(WebConfig.class)
public abstract class E2ETest {

    @Autowired
    DatabaseHelper databaseHelper;

    @LocalServerPort
    int port;

    @BeforeEach
    void setup() {
        databaseHelper.clear();
        RestAssured.port = port;
    }

    @TestConfiguration
    static class WebConfig {

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
