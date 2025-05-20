package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.YESTERDAY;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Nested
    @DisplayName("과거의 예약인지 체크할 수 있다.")
    public class isPastDateTime {

        @DisplayName("과거의 예약인 경우 true 리턴")
        @Test
        void isPastDateTime() {
            // given
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "회원", "설명", "섬네일");
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            Reservation reservation = new Reservation(1L, YESTERDAY, ReservationStatus.BOOKED, time, theme, member);

            // when
            boolean isPast = reservation.isPastDateTime();

            // then
            assertThat(isPast).isTrue();
        }

        @DisplayName("과거가 아닌 예약인 경우 false 리턴")
        @Test
        void isNotPastDateTime() {
            // given
            ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
            Theme theme = new Theme(1L, "회원", "설명", "섬네일");
            Member member = new Member(1L, Role.GENERAL, "회원", "test@test.com", "qwer1234!");
            Reservation reservation = new Reservation(1L, NEXT_DAY, ReservationStatus.BOOKED, time, theme, member);

            // when
            boolean isPast = reservation.isPastDateTime();

            // then
            assertThat(isPast).isFalse();
        }
    }
}
