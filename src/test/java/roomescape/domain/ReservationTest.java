package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixture;

class ReservationTest {

    @Test
    @DisplayName("과거 시간의 예약은 true를 반환한다")
    void isPastReturnsTrueForPastTime() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 1);
        ReservationTime time = ReservationTime.createNew(LocalTime.of(12, 0));

        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 1, 2, 0, 0);
        Clock clock = TestFixture.fixedClockAt(fixedDateTime);

        Reservation reservation = Reservation.createNew(
                TestFixture.createDefaultMember(),
                date,
                time,
                TestFixture.createDefaultTheme()
        );

        // when & then
        assertThat(reservation.isPast(clock)).isTrue();
    }

    @Test
    @DisplayName("미래 시간의 예약은 false를 반환한다")
    void isPastReturnsFalseForFutureTime() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 2);
        ReservationTime time = ReservationTime.createNew(LocalTime.of(12, 0));

        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 1, 1, 0, 0);
        Clock clock = TestFixture.fixedClockAt(fixedDateTime);

        Reservation reservation = Reservation.createNew(
                TestFixture.createDefaultMember(),
                date,
                time,
                TestFixture.createDefaultTheme()
        );

        // when & then
        assertThat(reservation.isPast(clock)).isFalse();
    }

    @Test
    @DisplayName("예약 시작까지 남은 시간을 분단위로 계산한다")
    void calculateRemainingMinutes() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 1);
        ReservationTime time = ReservationTime.createNew(LocalTime.of(12, 0));

        LocalDateTime fixedDateTime = LocalDateTime.of(2025, 1, 1, 11, 30);
        Clock clock = TestFixture.fixedClockAt(fixedDateTime);

        Reservation reservation = Reservation.createNew(
                TestFixture.createDefaultMember(),
                date,
                time,
                TestFixture.createDefaultTheme()
        );

        // when
        long minutes = reservation.calculateMinutesUntilStart(clock);

        // then
        assertThat(minutes).isEqualTo(30L);
    }
}
