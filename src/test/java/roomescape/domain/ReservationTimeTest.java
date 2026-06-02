package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomEscapeException;

class ReservationTimeTest {

    @Test
    void 예약_시간을_생성_할_때_시작_시간_정보가_없다면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> new ReservationTime(null))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("추가 할 예약 시작 시간 정보가 누락되었습니다");
    }

    @Test
    void 유효한_예약_시간을_생성할_수_있다() {
        // given
        LocalTime startAt = LocalTime.of(11, 0);

        // when
        ReservationTime reservationTime = new ReservationTime(startAt);

        // then
        assertThat(reservationTime)
                .extracting(ReservationTime::getStartAt, ReservationTime::getStatus)
                .containsExactly(startAt, TimeStatus.ACTIVE);
    }

    @Test
    void 입력받은_날짜와_예약_시간을_조합했을_때_과거라면_true를_반환한다() {
        // given: 무조건 확실한 과거 (어제 날짜)
        LocalDate pastDate = LocalDate.now().minusDays(1);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));

        // when
        boolean isPast = reservationTime.isPast(pastDate);

        // then
        assertThat(isPast).isTrue();
    }

    @Test
    void 입력받은_날짜와_예약_시간을_조합했을_때_미래라면_false를_반환한다() {
        // given: 무조건 확실한 미래 (내일 날짜)
        LocalDate futureDate = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));

        // when
        boolean isPast = reservationTime.isPast(futureDate);

        // then
        assertThat(isPast).isFalse();
    }

    @Test
    void 예약_시간을_비활성화_할_수_있다() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));

        // when
        time.deactivate();

        // then
        assertThat(time.isActive()).isFalse();
    }

    @Test
    void 비활성화된_예약_시간에_비활성화를_시도하면_예외가_발생한다() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        time.deactivate();

        // when
        assertThatThrownBy(time::deactivate)
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 비활성화 된 시간 정보입니다.");
    }

    @Test
    void 예약_시간을_활성화_할_수_있다() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));
        time.deactivate();

        // when
        time.activate();

        // then
        assertThat(time.isActive()).isTrue();
    }

    @Test
    void 활성화된_예약_시간에_활성화를_시도하면_예외가_발생한다() {
        // given
        ReservationTime time = new ReservationTime(LocalTime.of(11, 0));

        // when
        assertThatThrownBy(time::activate)
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 활성화 된 시간 정보입니다.");
    }
}
