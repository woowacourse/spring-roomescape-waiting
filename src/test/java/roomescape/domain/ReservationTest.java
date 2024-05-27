package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.fixture.MemberFixtures;
import roomescape.fixture.ThemeFixtures;
import roomescape.fixture.TimeSlotFixtures;

class ReservationTest {

    @DisplayName("예약 상태를 예약으로 수정한다")
    @CsvSource(value = {"BOOKING,BOOKING", "PENDING,BOOKING"})
    @ParameterizedTest
    void book(ReservationStatus given, ReservationStatus expected) {
        LocalDate now = LocalDate.now();
        Member daon = MemberFixtures.createAdminMemberDaon("123@email.com");
        Theme theme = ThemeFixtures.createDefaultTheme();
        TimeSlot time = TimeSlotFixtures.createTimeSlot(LocalTime.now());
        Reservation reservation = new Reservation(daon, now, time, theme, given);

        reservation.book();

        assertThat(reservation.getStatus()).isEqualTo(expected);
    }
}
