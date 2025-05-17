package roomescape.reservationtime.domain;

import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.reservationtime.fixture.ReservationTimeFixture;

class ReservationTimeTest {

    @Nested
    @DisplayName("재정의한 equals 기능")
    class equals {

        @DisplayName("두 객체의 ID를 비교하여 동일하다면 true를 반환한다.")
        @Test
        void equals_true_withEachIDIsValid() {
            // given
            ReservationTime reservationTime1 = new ReservationTime(1L, LocalTime.of(12, 1));
            ReservationTime reservationTime2 = new ReservationTime(1L, LocalTime.of(12, 1));

            // when
            boolean actual = reservationTime1.equals(reservationTime2);

            // then
            Assertions.assertThat(actual).isTrue();
        }

        @DisplayName("ID가 null인 두 객체의 equals()는 false를 반환한다.")
        @Test
        void equals_false_whenEachIdNull() {
            // given
            ReservationTime reservationTime1 = ReservationTimeFixture.create(LocalTime.of(11, 10));
            ReservationTime reservationTime2 = ReservationTimeFixture.create(LocalTime.of(11, 10));

            // when
            boolean actual = reservationTime1.equals(reservationTime2);

            // then
            Assertions.assertThat(actual).isFalse();
        }

        @DisplayName("두 객체 중 하나만 ID가 null일 때 equals()는 false를 반환한다.")
        @Test
        void equals_false_byOneIdNull() {
            // given
            ReservationTime reservationTime1 = ReservationTimeFixture.create(LocalTime.of(11, 10));
            ReservationTime reservationTime2 = new ReservationTime(1L, LocalTime.of(12, 1));

            // when
            boolean actual = reservationTime1.equals(reservationTime2);

            // then
            Assertions.assertThat(actual).isFalse();
        }
    }
}
