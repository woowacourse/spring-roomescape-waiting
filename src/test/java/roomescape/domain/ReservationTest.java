package roomescape.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        Member member = Member.create("듀이", Role.USER, "email@email.com", "1234");
        LocalDate date = LocalDate.now();
        ReservationTime time = ReservationTime.create(LocalTime.of(10, 0));
        Theme theme = Theme.create("공포", "공포테마", "공포.jpg");
        reservation = Reservation.create(member, date, time, theme);
    }

    @Test
    void 예약을_하면_상태가_예약이다() {
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void 에약을_취소하면_상태가_취소로_변경된다() {
        reservation.cancel();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }
}
