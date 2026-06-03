package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.assertion.ReservationTimeAssertions;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;

public class ReservationTimeAcceptanceTest extends AcceptanceTest {

    @Test
    void 예약_시간을_생성하고_삭제할_수_있다() {
        // given
        예약_시간을_생성한다();
        전체_예약_시간이_1건이다();

        // when
        예약_시간을_삭제한다();

        // then
        전체_예약_시간이_0건이다();
    }

    @Test
    void 예약_추가_삭제에_따라_해당_시간의_예약_가능_상태가_변경된다() {
        // given
        예약_시간과_테마를_생성한다();
        해당_시간에_예약_가능하다();

        // when
        예약자가_예약한다();

        // then
        해당_시간에_예약_불가능하다();

        // when
        예약자가_예약을_취소한다();

        // then
        해당_시간에_예약_가능하다();
    }

    private void 예약_시간을_생성한다() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
    }

    private void 예약_시간과_테마를_생성한다() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");
    }

    private void 예약자가_예약한다() {
        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
    }

    private void 예약_시간을_삭제한다() {
        ReservationTimeFixture.deleteReservationTime(1L);
    }

    private void 예약자가_예약을_취소한다() {
        ReservationFixture.deleteReservation(1L);
    }

    private void 전체_예약_시간이_0건이다() {
        ReservationTimeAssertions.checkAllReservationTimeSize(0);
    }

    private void 전체_예약_시간이_1건이다() {
        ReservationTimeAssertions.checkAllReservationTimeSize(1);
    }

    private void 해당_시간에_예약_가능하다() {
        ReservationTimeAssertions.checkAvailableReservation(NOW_DATE, 1L, true);
    }

    private void 해당_시간에_예약_불가능하다() {
        ReservationTimeAssertions.checkAvailableReservation(NOW_DATE, 1L, false);
    }
}