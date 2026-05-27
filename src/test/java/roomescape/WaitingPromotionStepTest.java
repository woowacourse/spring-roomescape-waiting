package roomescape;

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

        // when - 브라운이 예약 취소
        RestAssured.given()
                .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                .then().statusCode(204);

        // then
        // 1) 콘이 예약자가 됐다
        assert helper.findReservationOwner(FUTURE_DATE, timeId, themeId).equals("콘")
                : "콘이 예약자가 되어야 함";

        // 2) 콘의 대기는 삭제됐다
        assert !helper.existsWaiting(w1) : "콘의 대기는 사라져야 함";

        // 3) 모카는 순번 1
        assert helper.findWaitingOrder(w2) == 1 : "모카의 순번이 1";

        // 4) 핀은 순번 2
        assert helper.findWaitingOrder(w3) == 2 : "핀의 순번이 2";
    }

    @Test
    @DisplayName("대기가 없을 때 예약 취소는 그냥 취소만 된다")
    void 대기_없을때_단순_취소() {
        Long reservationId = helper.insertReservationAndReturnId("브라운", FUTURE_DATE, timeId, themeId);

        RestAssured.given()
                .when().delete("/user/reservations/" + reservationId + "?name=브라운")
                .then().statusCode(204);

        // 예약은 없어졌다
        assert helper.findReservationCount(FUTURE_DATE, timeId, themeId) == 0;
    }
}
