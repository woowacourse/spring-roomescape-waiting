package roomescape.unit.reserveticket;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.member.Reserver;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.domain.reserveticket.ReserveTicketFinder;
import roomescape.domain.theme.Theme;

class ReserveTicketFinderTest {

    @Test
    @DisplayName("예약 ID로 예약자를 찾을 수 있다")
    void reservation_id_로_member_를_찾을_수_있다() {
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Reservation reservation = new Reservation(1L, LocalDate.now(), time, theme, ReservationStatus.RESERVATION);
        Reserver reserver = new Reserver(1L, "username", "password", "name", null);
        ReserveTicket reserveTicket = new ReserveTicket(1L, reservation, reserver);
        ReserveTicketFinder reserveTicketFinder = new ReserveTicketFinder(List.of(reserveTicket));

        Reserver foundReserver = reserveTicketFinder.findReserverFromReservation(1L);

        assertThat(foundReserver).isEqualTo(reserver);
    }
}
