package roomescape.member.dto.response;

import java.time.LocalDate;
import roomescape.reservation.model.Reservation;

public record CreateReservationResponse(Long id,
                                        CreateMemberOfReservationResponse member,
                                        CreateThemeOfReservationResponse theme,
                                        LocalDate date,
                                        CreateTimeOfReservationResponse time
                                        ) {
    public static CreateReservationResponse from(final Reservation reservation) {
        return new CreateReservationResponse(reservation.getId(),
                CreateMemberOfReservationResponse.from(reservation.getMember()),
                CreateThemeOfReservationResponse.from(reservation.getTheme()),
                reservation.getDate(),
                CreateTimeOfReservationResponse.from(reservation.getReservationTime()));
    }
}
