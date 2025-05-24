package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_2;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_3;
import static roomescape.fixture.domain.ReservationTimeFixture.NOT_SAVED_RESERVATION_TIME_1;
import static roomescape.fixture.domain.ThemeFixture.NOT_SAVED_THEME_1;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

class ReservationSlotTest {

    @Test
    void 대기_상태인_예약을_반환한다() {
        // given
        Member member1 = NOT_SAVED_MEMBER_1();
        Member member2 = NOT_SAVED_MEMBER_2();
        Member member3 = NOT_SAVED_MEMBER_3();
        ReservationTime reservationTime = NOT_SAVED_RESERVATION_TIME_1();
        Theme theme = NOT_SAVED_THEME_1();

        ReservationSlot reservationSlot = new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme);

        reservationSlot.addReservation(new Reservation(1L, member1, reservationSlot));
        reservationSlot.addReservation(new Reservation(2L, member2, reservationSlot));
        reservationSlot.addReservation(new Reservation(3L, member3, reservationSlot));
        reservationSlot.assignConfirmedIfEmpty();

        // when
        List<Reservation> waitingReservations = reservationSlot.getWaitingReservations();

        // then
        assertAll(
                () -> assertThat(waitingReservations).hasSize(2),
                () -> assertThat(waitingReservations).extracting(Reservation::getMember)
                        .containsExactly(member1, member2)
        );
    }

    @Test
    void 제거하는_예약이_확정된_예약일경우_null로_변경된다() {
        // given
        Member member1 = NOT_SAVED_MEMBER_1();
        Member member2 = NOT_SAVED_MEMBER_2();
        Member member3 = NOT_SAVED_MEMBER_3();
        ReservationTime reservationTime = NOT_SAVED_RESERVATION_TIME_1();
        Theme theme = NOT_SAVED_THEME_1();

        ReservationSlot reservationSlot = new ReservationSlot(LocalDate.now().plusDays(1), reservationTime, theme);

        Reservation reservation1 = new Reservation(1L, member1, reservationSlot);
        Reservation reservation2 = new Reservation(2L, member2, reservationSlot);
        Reservation reservation3 = new Reservation(3L, member3, reservationSlot);
        reservationSlot.addReservation(reservation1);
        reservationSlot.addReservation(reservation2);
        reservationSlot.addReservation(reservation3);
        reservationSlot.assignConfirmedIfEmpty();

        // when
        reservationSlot.removeReservation(reservation1);

        // then
        assertAll(
                () -> assertThat(reservationSlot.getConfirmedReservation()).isNull(),
                () -> assertThat(reservationSlot.getAllReservations()).containsExactlyInAnyOrder(reservation2,
                        reservation3),
                () -> assertThat(reservationSlot.getAllReservations()).hasSize(2)
        );
    }

}
