package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;


public class AcceptanceTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setPort() {
        RestAssured.port = port;
    }
}
