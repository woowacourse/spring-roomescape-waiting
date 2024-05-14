package roomescape.reservationtime.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReservationTimeTest {

    @Test
    @DisplayName("예약 시간 생성 시 시작 시간이 빈 값인 경우 예외가 발생한다.")
    void createReservationTime() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간 생성 시 시작 시간은 필수입니다.");
    }

    @Nested
    class isSameTo {

        @Test
        @DisplayName("주어진 id값이 시간 객체의 id와 동일할 경우 참을 반환한다.")
        void isSameTo() {
            long sameTimeId = 1L;
            ReservationTime reservationTime = new ReservationTime(sameTimeId, LocalTime.parse("10:00"));
            assertTrue(reservationTime.isSameTo(sameTimeId));
        }

        @Test
        @DisplayName("주어진 id값이 시간 객체의 id와 동일하지 않는 경우 거짓을 반환한다.")
        void isSameTo_WhenNotSame() {
            ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
            assertFalse(reservationTime.isSameTo(2L));
        }
    }

    @Nested
    class isSameStartAt {

        @Test
        @DisplayName("주어진 시간과 시간 객체의 시작 시간이 동일할 경우 참을 반환한다.")
        void isSameStartAt() {
            LocalTime sameTime = LocalTime.parse("10:00");
            ReservationTime reservationTime = new ReservationTime(null, sameTime);
            assertTrue(reservationTime.isSameStartAt(sameTime));
        }

        @Test
        @DisplayName("주어진 시간과 시간 객체의 시작 시간이 동일하지 않는 경우 거짓을 반환한다.")
        void isSameStartAt_WhenNotSame() {
            ReservationTime reservationTime = new ReservationTime(null, LocalTime.parse("10:00"));
            assertFalse(reservationTime.isSameStartAt(LocalTime.parse("10:01")));
        }
    }

    @Nested
    class isBefore {
        @Test
        @DisplayName("주어진 시간보다 예약 시간이 이전인 경우 참을 반환한다.")
        void isBefore() {
            ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(1, 2));
            assertTrue(reservationTime.isNotAfter(LocalTime.of(1, 3)));
        }

        @Test
        @DisplayName("주어진 시간보다 예약 시간이 이후인 경우 거짓을 반환한다.")
        void isBefore_WhenReservationTimeIsAfter() {
            ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(1, 2));
            assertTrue(reservationTime.isNotAfter(LocalTime.of(1, 2)));
        }

        @Test
        @DisplayName("주어진 시간보다 예약 시간이 같은 시간인 경우 거짓을 반환한다.")
        void isBefore_WhenReservationTimeIsSame() {
            ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(1, 2));
            assertFalse(reservationTime.isNotAfter(LocalTime.of(1, 1)));
        }
    }
}
