package roomescape;

import static roomescape.fixture.MemberFixture.ADMIN_EMAIL;
import static roomescape.fixture.MemberFixture.ADMIN_PASSWORD;
import static roomescape.fixture.MemberFixture.MEMBER_EMAIL;
import static roomescape.fixture.MemberFixture.MEMBER_PASSWORD;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.login.dto.LoginRequest;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
public class ControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpControllerTest() {
        RestAssured.port = port;
    }

    public String getAdminCookie() {
        LoginRequest loginRequest = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);
        return RestAssured.given().log().all()
                .body(loginRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().header("Set-Cookie").split(";")[0];
    }

    public String getMemberCookie() {
        LoginRequest loginRequest = new LoginRequest(MEMBER_EMAIL, MEMBER_PASSWORD);
        return RestAssured.given().log().all()
                .body(loginRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().header("Set-Cookie").split(";")[0];
    }

}
