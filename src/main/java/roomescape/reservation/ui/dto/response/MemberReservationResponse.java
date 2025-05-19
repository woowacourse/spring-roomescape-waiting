package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static MemberReservationResponse from(final Reservation reservation) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getState().getDescription()
        );
    }
}
