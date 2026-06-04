package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.slot.Slot;
import roomescape.support.ControllerTestSupport;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingPromotionPolicy;

public class ReservationTransactionIntegrationTest extends ControllerTestSupport {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private WaitingPromotionPolicy waitingPromotionPolicy;

    @Test
    @DisplayName("대기 승격 중 실패하면 예약 삭제가 롤백된다.")
    void 예약_취소_트랜잭션_테스트() {
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

        Integer cancledReservationCount = jdbcTemplate.queryForObject(
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

        assertThat(cancledReservationCount).isEqualTo(1);
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
