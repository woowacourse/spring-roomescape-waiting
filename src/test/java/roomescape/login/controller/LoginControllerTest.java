package roomescape.login.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static roomescape.InitialMemberFixture.COMMON_PASSWORD;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_1;
import static roomescape.InitialMemberFixture.LOGIN_MEMBER_4;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class LoginControllerTest {

    @Test
    @DisplayName("저장된 회원 정보로 로그인을 시도하면 응답 쿠키에 토큰 값이 반환된다")
    void login() {
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", COMMON_PASSWORD.password());
        memberParam.put("email", LOGIN_MEMBER_1.getEmail().email());

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .cookie("token", notNullValue());
    }

    @Test
    @DisplayName("요청 쿠키에 담긴 토큰에 대응하는 회원 이름을 응답한다.")
    void getLoginMemberName() {
        //given
        Map<String, String> memberParam = new HashMap<>();
        memberParam.put("password", "password");
        memberParam.put("email", "admin@email.com");

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParam)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract().cookie("token");

        //when & then
        RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .body("name", equalTo(LOGIN_MEMBER_4.getName().name()));
    }
}
