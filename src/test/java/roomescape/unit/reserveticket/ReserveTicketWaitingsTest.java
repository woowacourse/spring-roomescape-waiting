package roomescape.unit.reserveticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Reserver;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reserveticket.ReservationWithWaitingRanks;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.domain.reserveticket.ReserveTicketFinder;
import roomescape.domain.reserveticket.ReserveTicketWaiting;
import roomescape.domain.reserveticket.ReserveTicketWaitings;
import roomescape.domain.theme.Theme;

class ReserveTicketWaitingsTest {

    @Test
    @DisplayName("예약 대기 목록을 생성한다")
    void createReserveTicketWaitings() {
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.now();

        Reservation reservation = new Reservation(1L, date, time, theme, ReservationStatus.RESERVATION);
        Reservation reservation2 = new Reservation(2L, date, time, theme, ReservationStatus.PREPARE);

        Reserver reserver = new Reserver(1L, "username", "password", "name", Role.USER);
        Reserver reserver2 = new Reserver(2L, "username2", "password2", "name2", Role.ADMIN);

        ReserveTicket reserveTicket = new ReserveTicket(1L, reservation, reserver);
        ReserveTicket reserveTicket2 = new ReserveTicket(2L, reservation2, reserver2);

        ReserveTicketFinder reserveTicketFinder = new ReserveTicketFinder(List.of(reserveTicket, reserveTicket2));
        ReservationWithWaitingRanks reservationWithWaitingRanks = new ReservationWithWaitingRanks(
                List.of(reservation, reservation2));

        ReserveTicketWaitings reserveTicketWaitings = new ReserveTicketWaitings(reserveTicketFinder,
                reservationWithWaitingRanks);
        List<ReserveTicketWaiting> result = reserveTicketWaitings.reserveTicketWaitings();
        
        assertThat(result).extracting(ReserveTicketWaiting::getName)
                .containsExactlyInAnyOrder("name", "name2");
    }

    @Test
    @DisplayName("빈 예약 대기 목록을 생성한다")
    void createEmptyReserveTicketWaitings() {
        ReserveTicketFinder reserveTicketFinder = new ReserveTicketFinder(List.of());
        ReservationWithWaitingRanks reservationWithWaitingRanks = new ReservationWithWaitingRanks(List.of());

        ReserveTicketWaitings reserveTicketWaitings = new ReserveTicketWaitings(reserveTicketFinder,
                reservationWithWaitingRanks);
        List<ReserveTicketWaiting> result = reserveTicketWaitings.reserveTicketWaitings();

        assertThat(result).isEmpty();
    }
}
