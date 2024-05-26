package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.response.CreateMemberOfReservationResponse;
import roomescape.reservation.model.Reservation;

public record ConfirmReservationResponse(Long id, CreateMemberOfReservationResponse member,
                                         LocalDate date,
                                         CreateTimeOfReservationsResponse time,
                                         CreateThemeOfReservationResponse theme) {
    public static ConfirmReservationResponse from(Reservation reservation) {
        return new ConfirmReservationResponse(
                reservation.getId(),
                CreateMemberOfReservationResponse.from(reservation.getMember()),
                reservation.getDate(),
                CreateTimeOfReservationsResponse.from(reservation.getReservationTime()),
                CreateThemeOfReservationResponse.from(reservation.getTheme())
        );
    }
}
