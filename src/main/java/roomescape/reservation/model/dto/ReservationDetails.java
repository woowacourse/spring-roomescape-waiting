package roomescape.reservation.model.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.member.model.Member;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;

@Builder
public record ReservationDetails(
        LocalDate date,
        ReservationTime reservationTime,
        ReservationTheme reservationTheme,
        Member member
        ) {

    public Reservation toReservation() {
        return Reservation.builder()
                .member(member)
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .build();
    }
}
