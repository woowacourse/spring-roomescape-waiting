package roomescape.controller.api;

import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import roomescape.service.dto.request.LoginRequest;
import roomescape.service.dto.response.MemberIdAndNameResponse;
import roomescape.util.TokenGenerator;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AuthApiControllerTest {

    @Test
    @DisplayName("올바른 로그인 정보를 입력할 시, 로그인에 성공한다.")
    void authenticatedMemberLogin_Success() {
        RestAssured.given().log().all()
                .body(new LoginRequest("user@naver.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("잘못된 로그인 정보를 입력할 시, 로그인에 실패한다.")
    void authenticatedMemberLogin_Failure() {
        RestAssured.given().log().all()
                .body(new LoginRequest("wrong@naver.com", "1234"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("로그인한 사용자의 인증 정보 조회 시, 성공한다.")
    void authenticatedMemberLoginCheck_Success() {
        MemberIdAndNameResponse response = RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeUserToken())
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200).extract().as(MemberIdAndNameResponse.class);

        Assertions.assertThat(response.name()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("로그인하지 않은 사용자의 인증 정보 조회 시, 실패한다.")
    void authenticatedMemberLoginCheck_Failure() {
        RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }
}
