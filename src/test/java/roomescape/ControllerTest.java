package roomescape;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.login.dto.LoginRequest;

import static roomescape.fixture.MemberFixture.ADMIN_MEMBER;
import static roomescape.fixture.MemberFixture.MEMBER_MEMBER;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpControllerTest() {
        RestAssured.port = port;
    }

    public String getAdminCookie() {
        LoginRequest loginRequest = new LoginRequest(ADMIN_MEMBER.getEmail(), ADMIN_MEMBER.getPassword());
        return RestAssured.given().log().all()
                .body(loginRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().header("Set-Cookie").split(";")[0];
    }

    public String getMemberCookie() {
        LoginRequest loginRequest = new LoginRequest(MEMBER_MEMBER.getEmail(), MEMBER_MEMBER.getPassword());
        return RestAssured.given().log().all()
                .body(loginRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().header("Set-Cookie").split(";")[0];
    }

}
