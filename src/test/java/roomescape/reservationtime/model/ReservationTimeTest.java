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
    @DisplayName("예약 시간 생성 실패: 시작 시간 없음")
    void createReservationTime() {
        assertThatThrownBy(() -> new ReservationTime(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간 생성 시 시작 시간은 필수입니다.");
    }

    @Nested
    class isSameTo {

        @Test
        @DisplayName("주어진 id값이 시간 객체의 id와 같음: 참")
        void isSameTo() {
            long sameTimeId = 1L;
            ReservationTime reservationTime = new ReservationTime(sameTimeId, LocalTime.parse("10:00"));
            ReservationTime sameTime = new ReservationTime(sameTimeId, LocalTime.parse("20:00"));
            assertTrue(reservationTime.isSameTo(sameTime));
        }

        @Test
        @DisplayName("주어진 id값이 시간 객체의 id와 다름: 거짓")
        void isSameTo_WhenNotSame() {
            ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
            ReservationTime sameTime = new ReservationTime(2L, LocalTime.parse("20:00"));
            assertFalse(reservationTime.isSameTo(sameTime));
        }
    }
}
