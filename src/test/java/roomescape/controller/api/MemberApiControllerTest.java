package roomescape.controller.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import roomescape.service.dto.request.SignupRequest;
import roomescape.util.TokenGenerator;

import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberApiControllerTest {

    @Test
    @DisplayName("유저 목록 조회 요청이 정상적으로 수행된다.")
    void selectMembers_Success() {
        RestAssured.given().log().all()
                .cookie("token", TokenGenerator.makeAdminToken())
                .when().get("/admin/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("유저 추가 요청이 정상적으로 수행된다.")
    void createMembers_Success() {
        RestAssured.given().log().all()
                .body(new SignupRequest("test@naver.com", "1234", "test"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 가입된 이메일인 경우 유저 추가 요청이 실패한다.")
    void createMembers_Failure() {
        RestAssured.given().log().all()
                .body(new SignupRequest("user@naver.com", "1234", "test"))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }
}
