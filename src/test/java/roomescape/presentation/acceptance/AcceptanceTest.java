package roomescape.presentation.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import roomescape.config.TestConfig;

@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
class AcceptanceTest {
    @LocalServerPort
    private int port;

    @BeforeEach
    void initializePort() {
        RestAssured.port = port;
    }
}
