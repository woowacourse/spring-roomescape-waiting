package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTest {

    @Test
    @DisplayName("예약 아이디와 얘약 일자, 예약 시간 및 테마가 같은 경우 동일하다")
    void isEqual() {
        Member member = new Member(1L, "user", "user@example.com", "password", Role.USER);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        Theme theme = new Theme(1L, "roomescape", "timeAttack", "timeAttack.jpg");

        Reservation reservation1 = new Reservation(1L, member, LocalDate.parse("2025-05-05"), reservationTime, theme);
        Reservation reservation2 = new Reservation(1L, member, LocalDate.parse("2025-05-05"), reservationTime, theme);

        assertThat(reservation1.equals(reservation2)).isTrue();
    }
}
