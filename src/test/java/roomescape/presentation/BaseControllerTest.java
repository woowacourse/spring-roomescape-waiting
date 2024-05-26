package roomescape.presentation;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.application.TokenProvider;
import roomescape.support.config.TestConfig;
import roomescape.support.extension.DatabaseClearExtension;

@SpringBootTest(
        classes = TestConfig.class,
        webEnvironment = WebEnvironment.RANDOM_PORT
)
@ExtendWith(DatabaseClearExtension.class)
public abstract class BaseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected TokenProvider tokenProvider;

    protected String token;

    @BeforeEach
    void environmentSetUp() {
        RestAssured.port = port;
    }
}
