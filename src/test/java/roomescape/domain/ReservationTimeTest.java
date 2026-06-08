package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomEscapeException;
import roomescape.support.TestDateTimes;

class ReservationTimeTest {

    @Test
    void 예약_시간을_생성_할_때_시작_시간_정보가_없다면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReservationTime.create(null))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("추가 할 예약 시작 시간 정보가 누락되었습니다");
    }

    @Test
    void 유효한_예약_시간을_생성할_수_있다() {
        // given
        LocalTime startAt = TestDateTimes.hour(11);

        // when
        ReservationTime reservationTime = ReservationTime.create(startAt);

        // then
        assertThat(reservationTime)
                .extracting(ReservationTime::getStartAt, ReservationTime::getStatus)
                .containsExactly(startAt, TimeStatus.ACTIVE);
    }

    @Test
    void 예약_시간을_비활성화_할_수_있다() {
        // given
        ReservationTime time = ReservationTime.create(TestDateTimes.hour(11));

        // when
        time.deactivate();

        // then
        assertThat(time.isActive()).isFalse();
    }

    @Test
    void 비활성화된_예약_시간에_비활성화를_시도하면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(TestDateTimes.hour(11));
        time.deactivate();

        // when
        assertThatThrownBy(time::deactivate)
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 비활성화 된 시간 정보입니다.");
    }

    @Test
    void 예약_시간을_활성화_할_수_있다() {
        // given
        ReservationTime time = ReservationTime.create(TestDateTimes.hour(11));
        time.deactivate();

        // when
        time.activate();

        // then
        assertThat(time.isActive()).isTrue();
    }

    @Test
    void 활성화된_예약_시간에_활성화를_시도하면_예외가_발생한다() {
        // given
        ReservationTime time = ReservationTime.create(TestDateTimes.hour(11));

        // when
        assertThatThrownBy(time::activate)
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 활성화 된 시간 정보입니다.");
    }
}
