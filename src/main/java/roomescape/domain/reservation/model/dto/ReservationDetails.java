package roomescape.domain.reservation.model.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.domain.reservation.model.entity.Reservation;
import roomescape.domain.reservation.model.entity.ReservationTheme;
import roomescape.domain.reservation.model.entity.ReservationTime;

@Builder
public record ReservationDetails(
        String memberName,
        Long memberId,
        LocalDate date,
        ReservationTime reservationTime,
        ReservationTheme reservationTheme
) {

    public Reservation toReservation() {
        return Reservation.builder()
                .name(memberName)
                .memberId(memberId)
                .date(date)
                .time(reservationTime)
                .theme(reservationTheme)
                .build();
    }
}
