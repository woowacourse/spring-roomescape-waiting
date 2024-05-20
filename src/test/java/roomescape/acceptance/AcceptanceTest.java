package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.application.config.TestConfig;

@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AcceptanceFixture.class)
@Sql("/truncate.sql")
public abstract class AcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected AcceptanceFixture fixture;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }
}
