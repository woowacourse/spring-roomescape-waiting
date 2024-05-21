package roomescape.integration;

import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import roomescape.helper.CookieProvider;
import roomescape.helper.DatabaseCleaner;
import roomescape.helper.DatabaseInitializer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTest {
    @LocalServerPort
    int port;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DatabaseCleaner databaseCleaner;

    @Autowired
    protected DatabaseInitializer databaseInitializer;

    @Autowired
    protected CookieProvider cookieProvider;

    @MockBean
    protected Clock clock;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.execute();
        databaseInitializer.execute();
        given(clock.instant()).willReturn(Instant.parse("1999-09-19T19:19:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
    }
}
