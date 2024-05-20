package roomescape.member.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import roomescape.auth.domain.Token;
import roomescape.auth.provider.CookieProvider;
import roomescape.model.IntegrationTest;

class MemberIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("가입한 회원들의 이름들을 가져올 수 있다.")
    void memberList() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/members")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("회원의 예약 내역들을 가져올 수 있다.")
    void memberReservationList() {
        Token token = tokenProvider.getAccessToken(1);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        RestAssured.given().log().all()
                .cookie(cookie.toString())
                .contentType(ContentType.JSON)
                .when().get("/member/reservations")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(3))
                .body("[0].id", equalTo(1))
                .body("[0].themeName", equalTo("polla"))
                .body("[0].date", equalTo("2024-04-30"))
                .body("[0].time", equalTo("15:40"))
                .body("[0].status", equalTo("예약"));

    }
}
