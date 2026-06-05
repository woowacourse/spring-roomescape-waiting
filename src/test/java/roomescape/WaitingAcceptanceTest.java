package roomescape;

import static org.hamcrest.Matchers.is;
import static roomescape.support.AcceptanceTestHelper.cancelReservation;
import static roomescape.support.AcceptanceTestHelper.createReservation;
import static roomescape.support.AcceptanceTestHelper.createTheme;
import static roomescape.support.AcceptanceTestHelper.createTime;
import static roomescape.support.AcceptanceTestHelper.createWaiting;
import static roomescape.support.AcceptanceTestHelper.findMyReservations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WaitingAcceptanceTest extends IntegrationTest {

    private static final String FUTURE_DATE = "2050-12-31";

    @Test
    @DisplayName("예약자가 예약을 취소하면 대기 1번이 예약으로 자동 승격된다")
    void 예약_취소_시_대기_자동_승격() {
        Long timeId = createTime("10:00");
        Long themeId = createTheme("테마A", "설명", "https://example.com/a.jpg");

        Long reservationId = createReservation("브라운", FUTURE_DATE, timeId, themeId);

        createWaiting("콘", FUTURE_DATE, timeId, themeId, 1);
        createWaiting("모카", FUTURE_DATE, timeId, themeId, 2);

        cancelReservation(reservationId, "브라운")
                .statusCode(204);

        findMyReservations("콘")
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("RESERVED"))
                .body("[0].date", is(FUTURE_DATE))
                .body("[0].time.id", is(timeId.intValue()))
                .body("[0].theme.id", is(themeId.intValue()));

        findMyReservations("모카")
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].status", is("WAITING"))
                .body("[0].waitingOrder", is(1));
    }

    @Test
    @DisplayName("한 사용자가 예약과 대기를 모두 가진 경우 내 예약 목록에서 함께 조회된다")
    void 예약과_대기_상태_조회() {
        Long reservationTimeId = createTime("10:00");
        Long waitingTimeId = createTime("11:00");
        Long themeId = createTheme("테마A", "설명", "https://example.com/a.jpg");

        createReservation("브라운", FUTURE_DATE, reservationTimeId, themeId);
        createReservation("콘", FUTURE_DATE, waitingTimeId, themeId);
        createWaiting("브라운", FUTURE_DATE, waitingTimeId, themeId, 1);

        findMyReservations("브라운")
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].status", is("RESERVED"))
                .body("[0].date", is(FUTURE_DATE))
                .body("[0].time.id", is(reservationTimeId.intValue()))
                .body("[0].theme.id", is(themeId.intValue()))
                .body("[1].status", is("WAITING"))
                .body("[1].waitingOrder", is(1))
                .body("[1].date", is(FUTURE_DATE))
                .body("[1].time.id", is(waitingTimeId.intValue()))
                .body("[1].theme.id", is(themeId.intValue()));
    }
}
