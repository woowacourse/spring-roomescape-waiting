package roomescape.auth.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class LogoutRestControllerTest {

    @Test
    void 로그아웃_요청시_token_쿠키가_삭제된다() {
        RestAssured.given().log().all()
                .cookie("token", "validToken")
                .when().post("/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .cookie("token", equalTo(""));
    }
}
