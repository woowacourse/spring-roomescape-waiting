package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.restassured.RestAssured;
import roomescape.domain.Member;
import roomescape.service.security.JwtProvider;

class AdminAcceptanceTest extends AcceptanceTest {
    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void 어드민_메인_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookie("token", jwtProvider.encode(new Member(2L, "aa", "aa@aa.aa", "aa")))
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 예약_관리_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookie("token", jwtProvider.encode(new Member(2L, "aa", "aa@aa.aa", "aa")))
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 시간_관리_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookie("token", jwtProvider.encode(new Member(2L, "aa", "aa@aa.aa", "aa")))
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }
}
