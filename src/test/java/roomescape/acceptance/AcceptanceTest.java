package roomescape.acceptance;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.service.dto.request.TokenRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AcceptanceTest {

    @LocalServerPort
    int port;

    protected static final TokenRequest ADMIN_TOKEN_REQUEST = new TokenRequest("password", "admin@email.com");
    protected static final TokenRequest USER_TOKEN_REQUEST = new TokenRequest("password", "asd@email.com");

    protected String adminToken;
    protected String userToken;

    @BeforeEach
    void setToken() {
        RestAssured.port = port;

        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(ADMIN_TOKEN_REQUEST)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        userToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(USER_TOKEN_REQUEST)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");
    }
}
