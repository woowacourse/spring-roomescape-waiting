package roomescape.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.controller.dto.request.LoginRequest;
import roomescape.auth.provider.JwtTokenProvider;
import roomescape.member.controller.dto.request.MemberRequest;
import roomescape.member.repository.JpaMemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @BeforeEach
    void setUP() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithSignup())
            .when().post("/members")
            .then().log().all()
            .statusCode(201);
    }

    @Test
    @DisplayName("이메일과 비밀번호 검증 후 토큰 생성")
    void createReservation() {
        String cookieHeader = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithLogin())
            .when().post("/login")
            .then().log().all()
            .statusCode(200)
            .extract()
            .header("Set-Cookie");

        assertThat(cookieHeader).isNotNull();
        assertThat(cookieHeader).contains("token=");
        assertThat(cookieHeader).contains("HttpOnly");
    }

    @Test
    @DisplayName("토큰에서 쿠키 정보를 추출하여 사용자 이름을 반환")
    void checkLogin() {
        String token = RestAssured.given()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithLogin())
            .when().post("/login")
            .then().log().all()
            .extract().response().getCookie("token");

        RestAssured.given().log().all()
            .cookie("token", token)
            .when().get("/login/check")
            .then().log().all()
            .statusCode(200)
            .body("name", is("Lemon"));
    }

    @Test
    @DisplayName("로그인 테스트")
    void loginTest() {
        // given
        MemberRequest signupRequest = new MemberRequest(
                "레몬",
                "suwon@naver.com",
                "123"
        );

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(signupRequest)
                .when().post("/members")
                .then().statusCode(201)
                .extract().response();

        LoginRequest tokenRequest = new LoginRequest("suwon@naver.com", "123");
        // when
        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(tokenRequest)
                .when().post("/login")
                .then().statusCode(200);
    }

    private Map<String, String> getTestParamsWithLogin() {
        return Map.of(
            "email", "sa123",
            "password", "na123"
        );
    }

    private Map<String, String> getTestParamsWithSignup() {
        return Map.of(
            "name", "Lemon",
            "email", "sa123",
            "password", "na123"
        );
    }
}
