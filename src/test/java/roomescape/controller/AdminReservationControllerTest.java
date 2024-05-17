package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.IntegrationTestSupport;

class AdminReservationControllerTest extends IntegrationTestSupport {

    @DisplayName("예약 내역을 필터링하여 조회한다.")
    @Test
    void findReservationByFilter() {
        Map<String, String> params = Map.of("themeId", "1",
                "memberId", "1",
                "dateFrom", "2024-05-04",
                "dateTo", "2024-05-04"
        );

        RestAssured.given().log().all()
                .cookies("token", ADMIN_TOKEN)
                .queryParams(params)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(2));
    }
}
