package roomescape.acceptance;

import org.junit.jupiter.api.Test;
import roomescape.acceptance.assertion.ReservationAssertions;
import roomescape.acceptance.fixture.ReservationFixture;
import roomescape.acceptance.fixture.ReservationTimeFixture;
import roomescape.acceptance.fixture.ThemeFixture;
import roomescape.domain.ReservationStatus;

public class ReservationAcceptanceTest extends AcceptanceTest {

    @Test
    void 예약을_생성하고_삭제할_수_있다() {
        // given
        예약_시간과_테마를_생성한다();
        예약자가_예약한다();
        전체_예약이_1건이다();

        // when
        예약자가_예약을_취소한다();

        // then
        전체_예약이_0건이다();
    }

    @Test
    void 이미_예약된_슬롯에_예약하면_대기로_등록된다() {
        // given
        예약_시간과_테마를_생성한다();
        예약자가_예약한다();

        // when
        대기자가_대기를_등록한다();

        // then
        대기자의_예약_상태가_대기중이다();
    }

    @Test
    void 대기를_직접_취소할_수_있다() {
        // given
        예약_시간과_테마를_생성한다();
        예약자가_예약한다();
        대기자가_대기를_등록한다();
        전체_예약이_2건이다();

        // when
        대기자가_대기를_취소한다();

        // then
        전체_예약이_1건이다();
    }

    @Test
    void 대기자가_있을때_예약자가_취소하면_첫번째_대기자가_결제대기로_승격된다() {
        // given
        예약_시간과_테마를_생성한다();
        예약자가_예약한다();
        첫번째_대기자가_대기를_등록한다();
        두번째_대기자가_대기를_등록한다();

        // when
        예약자가_예약을_취소한다();

        // then
        첫번째_대기자의_예약_상태가_결제대기로_변경된다();
    }

    private void 예약_시간과_테마를_생성한다() {
        ReservationTimeFixture.createReservationTime(FUTURE_TIME);
        ThemeFixture.createTheme("방탈출1", "방탈출1 설명", "theme/url.png");
    }

    private void 예약자가_예약한다() {
        ReservationFixture.createReservation("예약자", NOW_DATE, 1L, 1L);
    }

    private void 대기자가_대기를_등록한다() {
        ReservationFixture.createReservation("대기자", NOW_DATE, 1L, 1L);
    }

    private void 첫번째_대기자가_대기를_등록한다() {
        ReservationFixture.createReservation("예약자2", NOW_DATE, 1L, 1L);
    }

    private void 두번째_대기자가_대기를_등록한다() {
        ReservationFixture.createReservation("예약자3", NOW_DATE, 1L, 1L);
    }

    private void 예약자가_예약을_취소한다() {
        ReservationFixture.deleteReservation(1L);
    }

    private void 대기자가_대기를_취소한다() {
        ReservationFixture.deleteWait(1L);
    }

    private void 전체_예약이_0건이다() {
        ReservationAssertions.checkAllReservationSize(0);
    }

    private void 전체_예약이_1건이다() {
        ReservationAssertions.checkAllReservationSize(1);
    }

    private void 전체_예약이_2건이다() {
        ReservationAssertions.checkAllReservationSize(2);
    }

    private void 대기자의_예약_상태가_대기중이다() {
        ReservationAssertions.readMyName("대기자", 1, ReservationStatus.WAITING.name());
    }

    private void 첫번째_대기자의_예약_상태가_결제대기로_변경된다() {
        ReservationAssertions.readMyName("예약자2", 1, ReservationStatus.PENDING.name());
    }
}
