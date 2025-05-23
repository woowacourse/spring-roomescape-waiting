package roomescape.reservation.ui.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status,
        Long rank
) {

    public static MemberReservationResponse from(final Reservation reservation, final Long rank) {
        return new MemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getState().getDescription(),
                rank
        );
    }
}
