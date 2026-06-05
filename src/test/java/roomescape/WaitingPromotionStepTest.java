package roomescape;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.support.ReservationTestHelper;

public class WaitingPromotionStepTest extends IntegrationTest {

    private static final LocalDate FUTURE_DATE = LocalDate.of(2050, 12, 31);

    @Autowired
    private ReservationTestHelper helper;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        timeId = helper.insertTime(LocalTime.of(10, 0));
        themeId = helper.insertTheme("테마A", "설명", "url");
    }

    @Test
    @DisplayName("예약 취소 시 대기 1번이 예약으로 자동 승격되고 나머지 대기 순번이 당겨진다")
    void 자동_승격_시나리오() {
        Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE, timeId, themeId);
        Long w1 = helper.insertWaiting("콘", FUTURE_DATE, timeId, themeId, 1);
        Long w2 = helper.insertWaiting("모카", FUTURE_DATE, timeId, themeId, 2);
        Long w3 = helper.insertWaiting("핀", FUTURE_DATE, timeId, themeId, 3);

        // when
        RestAssured.given()
                .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                .then().statusCode(204);

        // then
        assertThat(helper.findReservationOwner(FUTURE_DATE, timeId, themeId)).isEqualTo("콘");
        assertThat(helper.existsWaiting(w1)).isFalse();
        assertThat(helper.findWaitingOrder(w2)).isEqualTo(1);
        assertThat(helper.findWaitingOrder(w3)).isEqualTo(2);
    }

    @Test
    @DisplayName("대기가 없을 때 예약 취소는 그냥 취소만 된다")
    void 대기_없을때_단순_취소() {
        Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE, timeId, themeId);

        RestAssured.given()
                .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                .then().statusCode(204);

        assertThat(helper.findReservationCount(FUTURE_DATE, timeId, themeId)).isEqualTo(0);
    }
}
