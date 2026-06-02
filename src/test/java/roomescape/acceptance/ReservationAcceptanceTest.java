package roomescape.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.acceptance.assertion.ReservationAssertions;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;
import roomescape.domain.ReservationStatus;

public class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("예약을 생성하고 삭제할 수 있다")
    void createAndDeleteReservation() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
        ReservationAssertions.checkAllReservationSize(1);

        ReservationFixture.deleteReservation(1L);
        ReservationAssertions.checkAllReservationSize(0);
    }

    @Test
    @DisplayName("이미 예약된 슬롯에 예약하면 대기로 등록된다")
    void createWaitWhenSlotAlreadyBooked() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
        ReservationFixture.createReservation("대기자", NOW_DATE, 1L, 1L);

        ReservationAssertions.readMyName("대기자", 1, ReservationStatus.WAITING.name());
    }

    @Test
    @DisplayName("대기를 직접 취소할 수 있다")
    void cancelWait() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
        ReservationFixture.createReservation("대기자", NOW_DATE, 1L, 1L);
        ReservationAssertions.checkAllReservationSize(2);

        ReservationFixture.deleteWait(1L);
        ReservationAssertions.checkAllReservationSize(1);
    }

    @Test
    @DisplayName("예약 취소 시 첫 번째 대기자가 자동으로 예약 확정된다")
    void reservationApiSuccessTest() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");

        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
        ReservationFixture.createReservation("예약자2", NOW_DATE, 1L, 1L);
        ReservationFixture.createReservation("예약자3", NOW_DATE, 1L, 1L);
        ReservationAssertions.readMyName("예약자2", 1, ReservationStatus.WAITING.name());

        ReservationFixture.deleteReservation(1L);
        ReservationAssertions.readMyName("예약자2", 1, ReservationStatus.CONFIRMED.name());
    }
}
