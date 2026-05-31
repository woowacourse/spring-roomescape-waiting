package roomescape.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.Map;

@ActiveProfiles("test")
@Import(TestTimeConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class ControllerTestSupport {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpRestAssuredPort() {
        RestAssured.port = port;
    }

    protected String loginToken(String name, String password) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("name", name, "password", password))
                .when().post("/api/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("data.accessToken");
    }

    protected String loginUserToken() {
        return loginToken("a", "test1");
    }

    protected String loginWaitingUserToken() {
        return loginToken("b", "test2");
    }

    protected String loginOtherUserToken() {
        return loginToken("c", "test3");
    }

    protected String loginManagerToken() {
        return loginToken("d", "test4");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
