package roomescape.unit.reserveticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reserveticket.ReservationWithWaitingRank;
import roomescape.domain.reserveticket.ReservationWithWaitingRanks;
import roomescape.domain.theme.Theme;

class ReservationWithWaitingRanksTest {

    @Test
    @DisplayName("예약 티켓 대기 목록을 생성한다.")
    void createReservationTicketWaitings() {
        Theme theme = new Theme(1L, "테마1", "테마1 설명", "테마1 썸네일");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2024, 3, 20);

        Reservation reservation = new Reservation(null, date, time, theme, ReservationStatus.RESERVATION);
        Reservation reservation2 = new Reservation(null, date, time, theme, ReservationStatus.PREPARE);
        Reservation reservation3 = new Reservation(null, date, time, theme, ReservationStatus.PREPARE);

        ReservationWithWaitingRanks reservationWithWaitingRanks = new ReservationWithWaitingRanks(
                List.of(reservation, reservation2, reservation3));
        List<ReservationWithWaitingRank> result = reservationWithWaitingRanks.getReservationWithRanks();

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
        List<Reservation> reservations = List.of();

        ReservationWithWaitingRanks reservationWithWaitingRanks = new ReservationWithWaitingRanks(reservations);
        List<ReservationWithWaitingRank> result = reservationWithWaitingRanks.getReservationWithRanks();

        assertThat(result).isEmpty();
    }
}
