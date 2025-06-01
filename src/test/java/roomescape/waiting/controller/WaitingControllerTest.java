package roomescape.waiting.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.fixture.LoginMemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.dto.LoginRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/test-data.sql")
class WaitingControllerTest {

    private String userCookie;

    @BeforeEach
    void loginAsUser() {
        Member user = LoginMemberFixture.getUser();

        userCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(user.getPassword(), user.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @DisplayName("예약 대기를 등록한다.")
    @Test
    void createWaiting() {
        Map<String, Object> params = new HashMap<>();
        params.put("date", "2030-08-05");
        params.put("theme", 1);
        params.put("time", 1);

        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/waitings")
                .then().log().all()
                .statusCode(200)
                .body("message", is("1번째 예약대기 되었습니다"));
    }

    @DisplayName("예약 대기를 삭제한다.")
    @Test
    void deleteWaiting() {
        RestAssured.given().log().all()
                .header("Cookie", userCookie)
                .when().delete("/waitings/1")
                .then().log().all()
                .statusCode(204);
    }
}