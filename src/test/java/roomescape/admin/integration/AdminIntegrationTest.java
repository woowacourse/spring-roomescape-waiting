package roomescape.admin.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.auth.domain.Token;
import roomescape.auth.provider.CookieProvider;
import roomescape.model.IntegrationTest;

public class AdminIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("관리자가 아닌 경우 관리자 예매를 시도하지 못한다.")
    void canNotAccessAdmin_WhenMember() {
        Token token = tokenProvider.getAccessToken(2);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(LocalDate.now().plusDays(1), 1L,
                1L, 2L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminReservationRequest)
                .cookie(cookie.toString())
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("관리자인 경우 정상적으로 예약할 수 있다.")
    void canAccessAdmin_WhenAdmin() {
        Token token = tokenProvider.getAccessToken(1);
        ResponseCookie cookie = CookieProvider.setCookieFrom(token);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(LocalDate.now().plusDays(1), 1L,
                1L, 1L);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(adminReservationRequest)
                .cookie(cookie.toString())
                .when().post("/admin/reservations")
                .then().log().all()
                .statusCode(201);
    }
}
