package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import roomescape.application.config.TestConfig;
import roomescape.support.DatabaseCleanerExtension;

@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AcceptanceFixture.class)
@ExtendWith(DatabaseCleanerExtension.class)
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
