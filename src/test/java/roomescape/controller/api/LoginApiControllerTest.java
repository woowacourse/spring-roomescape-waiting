package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import roomescape.controller.api.dto.request.MemberLoginRequest;
import roomescape.controller.api.dto.response.TokenLoginResponse;
import roomescape.service.MemberService;
import roomescape.service.dto.input.MemberCreateInput;
import roomescape.util.DatabaseCleaner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;

//@formatter:off
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginApiControllerTest {
    @Autowired
    MemberService memberService;
    @LocalServerPort
    int port;
    @Autowired
    DatabaseCleaner databaseCleaner;
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("/login POST 요청시 쿠키를 반환한다.")
    void response_user_reservation_page() {
        memberService.createMember(new MemberCreateInput("어드민", "admin@email.com", "password"));

        RestAssured.given().contentType(ContentType.JSON).body(new MemberLoginRequest("admin@email.com","password"))
                .when().post("/login")
                .then().log().all().cookie("token",notNullValue()).statusCode(200);
    }
    @Test
    @DisplayName("쿠키에 토큰을 가지고, /login/check GET 요청시 200 과 이름을 반환한다.")
    void response_user_name_when_check_with_cookie(){
        final Map<String, Object> member = new HashMap<>();
        member.put("name", "조이썬");
        member.put("email", "i894@naver.com");
        member.put("password", "password");

        RestAssured.given().contentType(ContentType.JSON).body(member)
                .when().post("/members")
                .then().statusCode(201);

        final var token = RestAssured.given().contentType(ContentType.JSON)
                .body(new MemberLoginRequest("i894@naver.com","password"))
                .when().post("/login")
                .then().cookie("token",notNullValue()).extract().cookie("token");

        final var result = RestAssured.given().cookie("token",token)
                .when().get("/login/check")
                .then().statusCode(200).extract().as(TokenLoginResponse.class);

        assertThat(result.name()).isEqualTo("조이썬");
    }
}
