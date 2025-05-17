package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Nested
    class SuccessTest {

        @Test
        @DisplayName("동일한 id를 가진 예약 시간은 동등하다")
        void reservation_times_with_same_id_are_equal() {
            ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
            ReservationTime time2 = new ReservationTime(1L, LocalTime.of(12, 0));

            assertThat(time1).isEqualTo(time2);
        }

        @Test
        @DisplayName("다른 id를 가진 예약 시간은 동등하지 않다")
        void reservation_times_with_different_id_are_not_equal() {
            ReservationTime time1 = new ReservationTime(1L, LocalTime.of(10, 0));
            ReservationTime time2 = new ReservationTime(2L, LocalTime.of(10, 0));

            assertThat(time1).isNotEqualTo(time2);
        }

        @Test
        @DisplayName("id가 null인 예약 시간은 동등하지 않다")
        void reservation_times_with_null_id_are_not_equal() {
            ReservationTime time1 = new ReservationTime(null, LocalTime.of(10, 0));
            ReservationTime time2 = new ReservationTime(null, LocalTime.of(10, 0));

            assertThat(time1).isNotEqualTo(time2);
        }
    }
}
