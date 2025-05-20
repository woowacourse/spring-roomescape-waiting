package roomescape.member.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;

@Sql("/member.sql")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MemberControllerTest {

    @Test
    void 유저_회원가입() {
        // given
        Map<String, Object> signup = Map.of("name", "newName", "email", "newEmail@naver.com", "password",
                "newPassword");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signup)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 모든_유저_조회() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
