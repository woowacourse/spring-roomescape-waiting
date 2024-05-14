package roomescape.member.controller;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.controller.dto.request.SignupRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberControllerTest {
    @DisplayName("회원가입에 성공한다.")
    @Test
    void signupSuccess() {
        SignupRequest request = new SignupRequest("anna", "anna@gmail.com", "password");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/members")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("anna"))
                .body("email", equalTo("anna@gmail.com"))
                .body("password", equalTo("password"));
    }

    @DisplayName("잘못된 이메일 입력으로 회원가입에 실패한다.")
    @Test
    void signupFailedWhenEnterWrongEmail() {
        Map<String, String> request = new HashMap<>();
        request.put("name", "anna");
        request.put("email", "parang");
        request.put("password", "password");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/members")
                .then()
                .assertThat()
                .statusCode(400);
    }
}
