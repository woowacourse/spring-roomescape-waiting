package roomescape.controller.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.auth.LoginRequestDto;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Sql("/sql/test-data.sql")
class AdminWaitingControllerTest {
    String loginToken;

    @BeforeEach
    void setUp() {
        LoginRequestDto loginRequestDto = new LoginRequestDto("hello@woowa.com", "password");

        Map<String, String> cookies = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequestDto)
                .when().post("/login")
                .getCookies();

        //로그인 토큰
        loginToken = cookies.get("token");
    }

    @DisplayName("예약을 삭제한 경우 첫번 째 예약대기가 예약이 된다")
    @Test
    void replaceReservation(){
        RestAssured.given().cookie("token", loginToken).log().all()
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("",hasSize(2));

        RestAssured.given().cookie("token", loginToken).log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().cookie("token", loginToken).log().all()
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("",hasSize(1));
    }
}
