package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.WaitingException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.theme.domain.Theme;

public class WaitingTest {

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        // given
        final ReservationTime reservationTime = null;
        final Member member = new Member("우가", "우가@naver.com", "1234", Role.USER);
        final Theme theme = new Theme("우가", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 4, 24);

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Waiting(member, reservationTime, theme, date);
        }).isInstanceOf(WaitingException.class);
    }

    @Test
    void 멤버가_null이면_예외가_발생한다() {
        // given
        final Member member = null;
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("우가", "설명", "썸네일");
        final LocalDate date = LocalDate.of(2026, 4, 24);

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Waiting(member, reservationTime, theme, date);
        }).isInstanceOf(WaitingException.class);
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        // given
        final Member member = new Member("우가", "우가@naver.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = null;
        final LocalDate date = LocalDate.of(2026, 4, 24);

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Waiting(member, reservationTime, theme, date);
        }).isInstanceOf(WaitingException.class);
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // given
        final Member member = new Member("우가", "우가@naver.com", "1234", Role.USER);
        final ReservationTime reservationTime = new ReservationTime(LocalTime.of(20, 20));
        final Theme theme = new Theme("우가", "설명", "썸네일");
        final LocalDate date = null;

        // when & then
        Assertions.assertThatThrownBy(() -> {
            new Waiting(member, reservationTime, theme, date);
        }).isInstanceOf(WaitingException.class);
    }
}
