package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.theme.domain.Theme;

class ReservationTest {

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // given
        final Member member = new Member(1L, "이스트", "east@email.com", "1234", Role.ADMIN);
        final LocalDate date = null;
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, date, reservationTime, theme);
        }).isInstanceOf(ReservationException.class);
    }

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        // given
        final Member member = new Member(1L, "이스트", "east@email.com", "1234", Role.ADMIN);
        final LocalDate date = LocalDate.of(2025, 4, 24);
        final ReservationTime reservationTime = null;
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, date, reservationTime, theme);
        }).isInstanceOf(ReservationException.class);
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        // given
        final Member member = new Member(1L, "이스트", "east@email.com", "1234", Role.ADMIN);
        final LocalDate date = LocalDate.of(2025, 4, 24);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = null;

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, date, reservationTime, theme);
        }).isInstanceOf(ReservationException.class);
    }

    @Test
    void 날짜가_현재날짜보다_이전_날짜이면_예외가_발생한다() {
        // given
        final Member member = new Member(1L, "이스트", "east@email.com", "1234", Role.ADMIN);
        final LocalDate date = LocalDate.of(2020, 4, 24);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        final Theme theme = new Theme("헤일러", "헤일러 설명", "헤일러 썸네일");

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Reservation(member, date, reservationTime, theme);
        }).isInstanceOf(ReservationException.class);
    }
}
