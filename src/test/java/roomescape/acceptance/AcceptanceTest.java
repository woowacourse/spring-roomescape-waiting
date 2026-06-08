package roomescape.acceptance;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.support.SpringTestBase;

public class AcceptanceTest extends SpringTestBase {

    public static final String NOW_DATE = "2026-05-02";
    public static final String FUTURE_TIME = "10:00";

    @LocalServerPort
    private int port;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = port;
    }
}
