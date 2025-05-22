package roomescape.domain;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    @Test
    void 예약을_하면_예약상태가_변경된다() {
        Member member = Member.create("듀이", Role.USER, "email@email.com", "1234");
        LocalDate date = LocalDate.now();
        ReservationTime time = ReservationTime.create(LocalTime.of(10, 0));
        Theme theme = Theme.create("공포", "공포테마", "공포.jpg");

        Reservation reservation = Reservation.create(member, date, time, theme);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }
}
