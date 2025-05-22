package roomescape.domain.reserveticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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
import roomescape.domain.theme.Theme;
import roomescape.service.reserveticket.ReserveTicketWaiting;

class ReservationTicketWaitingsTest {

    @Test
    @DisplayName("예약 티켓 대기 목록을 생성한다.")
    void createReservationTicketWaitings() {
        Reserver member1 = new Reserver(1L, "member1", "password1", "member1@email.com", Role.USER);
        Reserver member2 = new Reserver(2L, "member2", "password2", "member2@email.com", Role.USER);
        Reserver member3 = new Reserver(3L, "member3", "password3", "member3@email.com", Role.USER);

        Theme theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2024, 3, 20);

        Reservation reservation = new Reservation(null, date, time, theme, ReservationStatus.RESERVATION);

        ReserveTicket reserveTicket1 = new ReserveTicket(1L, reservation, member1);
        ReserveTicket reserveTicket2 = new ReserveTicket(2L, reservation, member2);
        ReserveTicket reserveTicket3 = new ReserveTicket(3L, reservation, member3);

        List<ReserveTicket> reserveTickets = List.of(reserveTicket1, reserveTicket2, reserveTicket3);

        ReservationTicketWaitings reservationTicketWaitings = new ReservationTicketWaitings(reserveTickets);
        List<ReserveTicketWaiting> result = reservationTicketWaitings.reserveTicketWaitings();

        assertThat(result).hasSize(3);
        assertAll(
                () -> assertThat(result.get(0).getWaitRank()).isEqualTo(1),
                () -> assertThat(result.get(1).getWaitRank()).isEqualTo(2),
                () -> assertThat(result.get(2).getWaitRank()).isEqualTo(3)
        );
    }

    @Test
    @DisplayName("빈 예약 티켓 목록으로 대기 목록을 생성한다.")
    void createReservationTicketWaitingsWithEmptyList() {
        List<ReserveTicket> reserveTickets = List.of();

        ReservationTicketWaitings reservationTicketWaitings = new ReservationTicketWaitings(reserveTickets);
        List<ReserveTicketWaiting> result = reservationTicketWaitings.reserveTicketWaitings();

        assertThat(result).isEmpty();
    }
}
