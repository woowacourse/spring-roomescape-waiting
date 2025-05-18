package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MemberControllerTest {

    @Test
    @DisplayName("회원가입 페이지에서 사용자 추가")
    void createMember() {
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(getTestParamsWithMember())
            .when().post("/members")
            .then().log().all()
            .statusCode(201);
    }

    private Map<String, String> getTestParamsWithMember() {
        return Map.of(
            "name", "이름",
            "email", "이메일",
            "password", "비밀번호"
        );
    }
}
