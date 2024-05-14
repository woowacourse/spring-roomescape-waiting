package roomescape.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import roomescape.helper.DatabaseCleaner;
import roomescape.helper.DatabaseInitializer;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AcceptanceTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected DatabaseCleaner databaseCleaner;
    @Autowired
    protected DatabaseInitializer databaseInitializer;
    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.execute();
        databaseInitializer.execute();
    }
}
