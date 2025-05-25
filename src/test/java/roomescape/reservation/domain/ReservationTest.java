package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.fixture.entity.MemberFixture;
import roomescape.fixture.entity.ReservationDateTimeFixture;
import roomescape.fixture.entity.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.InvalidStatusTransitionException;
import roomescape.theme.domain.Theme;

public class ReservationTest {

    private final Member member = MemberFixture.createUser();
    private final Theme theme = ThemeFixture.create();
    private final ReservationDateTime reservationDateTime = ReservationDateTimeFixture.create();

    @Test
    void 대기상태에서_예약상태로_전환된다() {
        Reservation reservation = Reservation.waiting(member, reservationDateTime, theme);

        assertThatCode(reservation::reserved)
                .doesNotThrowAnyException();
    }

    @Test
    void 대기상태가_아니면_예약상태로_전환시_예외가_발생한다() {
        Reservation reservation = Reservation.reserve(member, reservationDateTime, theme);

        assertThatThrownBy(reservation::reserved)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void 예약상태에서_예약취소상태로_전환된다() {
        Reservation reservation = Reservation.reserve(member, reservationDateTime, theme);

        assertThatCode(reservation::cancelReservation)
                .doesNotThrowAnyException();
    }

    @Test
    void 예약상태가_아니면_예약취소시_예외가_발생한다() {
        Reservation reservation = Reservation.waiting(member, reservationDateTime, theme);

        assertThatThrownBy(reservation::cancelReservation)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void 대기상태에서_대기취소상태로_전환된다() {
        Reservation reservation = Reservation.waiting(member, reservationDateTime, theme);

        assertThatCode(reservation::cancelWaiting)
                .doesNotThrowAnyException();
    }

    @Test
    void 대기상태가_아니면_대기취소시_예외가_발생한다() {
        Reservation reservation = Reservation.reserve(member, reservationDateTime, theme);

        assertThatThrownBy(reservation::cancelWaiting)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }
}
