package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;

class ReservationTest {

    @Test
    @DisplayName("현재 날짜보다 이전 날짜로 예약시 예외가 발생한다.")
    void createReservationByLastDate() {
        Theme theme = new Theme(new ThemeName("공포"), new Description("무서운 테마"), "http://asdf.jpg");
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());

        assertThatThrownBy(() -> new Reservation(
                new Member(new MemberName("카키"), "hogi@email.com", "1234"),
                LocalDate.now().minusDays(1),
                theme,
                reservationTime)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
