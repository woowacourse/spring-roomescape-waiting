package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @DisplayName("ReservationDetails가 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidReservationDetailsTest() {
        // given
        Member member = new Member(1L, "가이온", "user@gmail.com", "wooteco7", Role.USER);

        // when & then
        assertThatThrownBy(() -> new Reservation(1L, member, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("멤버가 존재하지 않는 경우 생성할 수 없다.")
    @Test
    void invalidReservationNameTest() {
        // given
        Theme theme = new Theme(1L, "우테코", "방탈출", "https://");
        ReservationDetails details = new ReservationDetails(LocalDate.now(), new ReservationTime(1L, LocalTime.now()), theme);

        // when & then
        assertThatThrownBy(() -> new Reservation(1L, null, details))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
