package roomescape.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.controller.member.dto.CookieMemberResponse;
import roomescape.controller.member.dto.MemberLoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserPageControllerTest {

    static final MemberLoginRequest REQUEST = new MemberLoginRequest("jinwuo0925@gmail.com",
            "1111");

    @LocalServerPort
    int port;

    @BeforeEach
    void beforeEach() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("사용자 로그인")
    void showUserPage() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(REQUEST)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("사용자 정보 확인")
    void checkAdmin() {
        final String accessToken = RestAssured.given().log().all()
                .body(REQUEST)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

        final CookieMemberResponse memberResponse = RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200).extract().as(CookieMemberResponse.class);

        assertThat(memberResponse.name()).isEqualTo("제제");
    }

    @Test
    @DisplayName("로그인 후 로그아웃")
    void logout() {
        final String accessToken = RestAssured.given().log().all()
                .body(REQUEST)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then().log().cookies().extract().cookie("token");

        RestAssured.given().log().all()
                .cookie("token", accessToken)
                .when().post("/logout")
                .then().log().all()
                .statusCode(200);
    }
}
