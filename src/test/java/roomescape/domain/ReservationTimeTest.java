package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.ReservationTime;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTimeTest {

    @Test
    @DisplayName("새로운 예약을 위한 시간을 성공적으로 생성한다.")
    void createReservationTimeTest() {
        // given
        LocalTime startAt = LocalTime.of(10, 0);

        // when
        ReservationTime reservationTime = ReservationTime.create(startAt);

        // then
        assertThat(reservationTime.getId()).isNull();
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("동일한 ID를 가진 예약 시간은 같은 객체로 판단한다.")
    void equalsAndHashCodeTest() {
        // given
        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(13, 0);

        ReservationTime reservationTime1 = ReservationTime.from(1L, time1);
        ReservationTime reservationTime2 = ReservationTime.from(1L, time2);

        // when & then
        assertThat(reservationTime1).isEqualTo(reservationTime2);
        assertThat(reservationTime1.hashCode()).isEqualTo(reservationTime2.hashCode());
    }
}
