package roomescape.web;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.token.TokenProvider;
import roomescape.member.model.MemberRole;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminWebControllerTest {

    @Autowired
    private TokenProvider tokenProvider;

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    public void setup() {
        RestAssured.port = randomServerPort;
    }

    @DisplayName("/admin으로 요청하면 200응답이 넘어온다.")
    @Test
    void requestAdminPageTest() {
        RestAssured.given().log().all()
                .cookie("token", createAdminAccessToken())
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자가 아닌 클라이언트가 /admin으로 요청하면 403응답이 넘어온다.")
    @Test
    void requestAdminPageWhoNotAdminTest() {
        RestAssured.given().log().all()
                .cookie("token", createUserAccessToken())
                .when().get("/admin")
                .then().log().all()
                .statusCode(403)
                .body("message", is("유효하지 않은 권한 요청입니다."));
    }

    @DisplayName("/admin/reservation으로 요청하면 200응답이 넘어온다.")
    @Test
    void requestReservationPageTest() {
        RestAssured.given().log().all()
                .cookie("token", createAdminAccessToken())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("관리자가 아닌 클라이언트가 /admin/reservation으로 요청하면 403응답이 넘어온다.")
    @Test
    void requestReservationPageWhoNotAdminTest() {
        RestAssured.given().log().all()
                .cookie("token", createUserAccessToken())
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(403)
                .body("message", is("유효하지 않은 권한 요청입니다."));
    }

    private String createUserAccessToken() {
        return tokenProvider.createToken(3L, MemberRole.USER);
    }

    private String createAdminAccessToken() {
        return tokenProvider.createToken(1L, MemberRole.ADMIN);
    }
}
