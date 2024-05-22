package roomescape.service.dto.response;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record UserReservationResponse(
        long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public UserReservationResponse(
            long id,
            Reservation reservation,
            Member member
    ) {
        this(
                id,
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                ReservationStatus.messageOf(reservation, member)
        );
    }
}
