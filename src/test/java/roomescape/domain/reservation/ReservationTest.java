package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReservationTest {

    @Test
    void 주어진_사용자의_예약인지_확인() {
        // given
        Member member = new Member("name", "email@test.com", "password123", Role.ADMIN);
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = new Theme("테마", "주어진 사용자의 예약인지 확인합니다.", "썸네일");
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());
        Reservation reservation = new Reservation(date, reservationTime, theme, member);

        // when
        boolean result = reservation.isOwner(member);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 지나간_시간에_대한_예약_생성_시_예외_발생() {
        // given
        Member member = new Member("name", "email@test.com", "password123", Role.ADMIN);
        LocalDate date = LocalDate.now().minusDays(1);
        Theme theme = new Theme("테마", "주어진 사용자의 예약인지 확인합니다.", "썸네일");
        ReservationTime reservationTime = new ReservationTime(LocalTime.now());

        // when, then
        assertThatThrownBy(() -> new Reservation(date, reservationTime, theme, member))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
