package roomescape.acceptance;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.security.JwtProvider;

class AdminAcceptanceTest extends AcceptanceTest {
    private static final String TOKEN = new JwtProvider().encode(new Member(1L, "a", "b", "C", Role.ADMIN));

    @Test
    void 어드민_메인_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookie("token", TOKEN)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 예약_관리_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookie("token", TOKEN)
                .when().get("/admin/reservation")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 시간_관리_페이지를_응답할_수_있다() {
        RestAssured.given().log().all()
                .cookie("token", TOKEN)
                .when().get("/admin/time")
                .then().log().all()
                .statusCode(200);
    }
}
