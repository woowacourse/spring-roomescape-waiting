package roomescape.waiting.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
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
class AdminWaitingControllerTest {

    String adminCookie;

    @BeforeEach
    void setCookies() {
        Member admin = LoginMemberFixture.getAdmin();

        adminCookie = RestAssured
                .given().log().all()
                .body(new LoginRequest(admin.getPassword(), admin.getEmail()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    @DisplayName("전체 예약대기 목록을 받는다.")
    @Test
    void getAllWaiting() {
        RestAssured.given().log().all()
                .header("Cookie", adminCookie)
                .when().get("/admin/waitings")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(1));
    }

    @DisplayName("예약 대기를 승인한다")
    @Test
    void acceptWaiting() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "APPROVED");

        RestAssured.given().log().all()
                .header("Cookie", adminCookie)
                .params(params)
                .when().post("/admin/waitings/1")
                .then().log().all()
                .statusCode(200)
                .body("message", is("예약 대기를 승인하였습니다."));
    }

    @DisplayName("예약 대기를 거부한다")
    @Test
    void denyWaiting() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "DENIED");

        RestAssured.given().log().all()
                .header("Cookie", adminCookie)
                .params(params)
                .when().post("/admin/waitings/1")
                .then().log().all()
                .statusCode(200)
                .body("message", is("예약 대기를 거절하였습니다."));
    }
}