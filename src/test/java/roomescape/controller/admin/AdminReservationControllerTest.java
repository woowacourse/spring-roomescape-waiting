package roomescape.controller.admin;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.service.dto.response.ReservationResponses;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdminReservationControllerTest extends IntegrationTestSupport {

    @DisplayName("예약 내역을 필터링하여 조회한다.")
    @Test
    void findReservationByFilter() {
        Map<String, String> params = Map.of(
                "themeId", "1",
                "memberId", "1",
                "dateFrom", "2024-05-04",
                "dateTo", "2024-05-04"
        );

        int size = RestAssured.given().log().all()
                .cookies("token", ADMIN_TOKEN)
                .queryParams(params)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", ReservationResponses.class)
                .reservationResponses()
                .size();

        assertThat(size).isEqualTo(2);
    }
}
