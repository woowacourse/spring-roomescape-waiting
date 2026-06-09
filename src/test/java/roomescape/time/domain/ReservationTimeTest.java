package roomescape.time.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("성공적으로 시간 도메인 객체를 생성한다.")
    void of_validTime_returnsReservationTime() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        // when
        ReservationTime reservationTime = new ReservationTime(time);

        // then
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservationTime.getId()).isNull();
    }

    @Test
    @DisplayName("생성된 시간 객체의 필드 값을 확인한다.")
    void constructor_validInput_storesFields() {
        // given
        LocalTime now = LocalTime.now();
        ReservationTime reservationTime = new ReservationTime(1L, now);

        // then
        assertThat(reservationTime.getId()).isEqualTo(1L);
        assertThat(reservationTime.getStartAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("아이디가 다르면 시간이 같아도 다른 객체로 인식한다")
    void equals_false_when_id_is_diff() {
        // given
        LocalTime testTime = LocalTime.of(10, 0);
        ReservationTime reservationTime1 = new ReservationTime(1L, testTime);
        ReservationTime reservationTime2 = new ReservationTime(2L, testTime);

        // when, then
        Assertions.assertNotEquals(reservationTime1, reservationTime2);
    }
}
