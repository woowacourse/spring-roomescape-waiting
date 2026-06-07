package roomescape.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTimeTest {

    @Test
    void ReservationTime_객체_생성() {
        final LocalTime startAt = LocalTime.of(10, 0);
        final LocalTime endAt = LocalTime.of(11, 0);

        final ReservationTime reservationTime = ReservationTime.create(startAt, endAt);

        assertThat(reservationTime.getStartAt()).isEqualTo(startAt);
        assertThat(reservationTime.getEndAt()).isEqualTo(endAt);
    }

    @Test
    void 시작_시간이_null이면_예외발생() {
        assertThatThrownBy(() -> ReservationTime.create(null, LocalTime.of(11, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 종료_시간이_null이면_예외발생() {
        assertThatThrownBy(() -> ReservationTime.create(LocalTime.of(10, 0), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 시작_시간이_종료_시간보다_늦거나_같으면_예외발생() {
        final LocalTime startAt = LocalTime.of(11, 0);
        final LocalTime endAt = LocalTime.of(10, 0);

        assertThatThrownBy(() -> ReservationTime.create(startAt, endAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 현재_시간보다_이전인지_확인() {
        final ReservationTime pastTime = ReservationTime.create(LocalTime.now().minusHours(1), LocalTime.now());
        final ReservationTime futureTime = ReservationTime.create(LocalTime.now().plusHours(1), LocalTime.now().plusHours(2));

        assertThat(pastTime.isBefore()).isTrue();
        assertThat(futureTime.isBefore()).isFalse();
    }
}
