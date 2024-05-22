package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.response.CreateMemberOfReservationResponse;
import roomescape.reservation.model.Reservation;

// TODO: 전체 DTO 필드 줄바꿈
public record CreateReservationResponse(Long id,
                                        CreateMemberOfReservationResponse member,
                                        LocalDate date,
                                        CreateTimeOfReservationsResponse time,
                                        CreateThemeOfReservationResponse theme) {
    public static CreateReservationResponse from(final Reservation reservation) {
        return new CreateReservationResponse(
                reservation.getId(),
                CreateMemberOfReservationResponse.from(reservation.getMember()),
                reservation.getDate(),
                CreateTimeOfReservationsResponse.from(reservation.getReservationTime()),
                CreateThemeOfReservationResponse.from(reservation.getTheme())
        );
    }
}
