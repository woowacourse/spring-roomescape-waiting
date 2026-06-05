package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.slot.domain.Slot;
import roomescape.support.ControllerTestSupport;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingPromotionPolicy;

public class ReservationTransactionIntegrationTest extends ControllerTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private WaitingPromotionPolicy waitingPromotionPolicy;

    @Test
    @DisplayName("대기 승격 중 실패하면 예약 삭제가 롤백된다.")
    void 대기_승격_중_실패하면_예약_삭제가_롤백된다() {
        String reservationUserToken = loginUserToken();
        String waitingUserToken = loginWaitingUserToken();

        createWaiting(waitingUserToken);

        given(waitingPromotionPolicy.promote(any(Waiting.class), any(Slot.class)))
                .willThrow(new RuntimeException("승격 실패"));

        RestAssured.given().log().all()
                .header("Authorization", bearer(reservationUserToken))
                .pathParam("id", 1)
                .when().delete("/api/user/reservations/{id}")
                .then().log().all()
                .statusCode(500);

        Integer canceledReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where id = 1",
                Integer.class
        );

        Integer waitingCount = jdbcTemplate.queryForObject(
                "select count(*) from waiting where member_id = 2 and slot_id = 1",
                Integer.class
        );

        Integer promotedReservationCount = jdbcTemplate.queryForObject(
                "select count(*) from reservation where member_id = 2 and slot_id = 1",
                Integer.class
        );

        assertThat(canceledReservationCount).isEqualTo(1);
        assertThat(waitingCount).isEqualTo(1);
        assertThat(promotedReservationCount).isEqualTo(0);
    }

    private void createWaiting(String accessToken) {
        RestAssured.given().log().all()
                .header("Authorization", bearer(accessToken))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2026-05-05",
                        "timeId", 1,
                        "themeId", 1
                ))
                .when().post("/api/user/waitings")
                .then().log().all()
                .statusCode(201);
    }
}
