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
class UserWebControllerTest {

    @Autowired
    private TokenProvider tokenProvider;

    @LocalServerPort
    int randomServerPort;

    @BeforeEach
    public void setup() {
        RestAssured.port = randomServerPort;
    }

    @DisplayName("/로 요청하면 200응답이 넘어온다.")
    @Test
    void requestPopularThemePageTest() {
        RestAssured.given().log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("/reservation으로 요청하면 200응답이 넘어온다.")
    @Test
    void requestUserReservationPageTest() {
        RestAssured.given().log().all()
                .cookie("token", createUserAccessToken())
                .when().get("/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("로그인 하지 않은 상태로 /reservation을 요청하면 에러 코드가 응답된다.")
    @Test
    void requestUserReservationPageNotLoginTest() {
        RestAssured.given().log().all()
                .when().get("/reservation")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증되지 않은 요청입니다."));
    }

    private String createUserAccessToken() {
        return tokenProvider.createToken(3L, MemberRole.USER);
    }
}
