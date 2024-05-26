package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.model.Reservation;

public record FindReservationResponse(Long id,
                                      FindMemberOfReservationResponse member,
                                      LocalDate date,
                                      FindTimeOfReservationsResponse time,
                                      FindThemeOfReservationResponse theme) {
    public static FindReservationResponse from(Reservation reservation) {
        return new FindReservationResponse(
                reservation.getId(),
                FindMemberOfReservationResponse.from(reservation.getMember()),
                reservation.getDate(),
                FindTimeOfReservationsResponse.from(reservation.getReservationTime()),
                FindThemeOfReservationResponse.from(reservation.getTheme())
        );
    }
}
