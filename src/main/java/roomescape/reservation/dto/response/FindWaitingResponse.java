package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.model.Reservation;

public record FindWaitingResponse(
        Long id,
        FindMemberOfWaitingResponse member,
        LocalDate date,
        FindTimeOfWaitingResponse time,
        FindThemeOfWaitingResponse theme) {

    public static FindWaitingResponse from(final Reservation reservation) {
        return new FindWaitingResponse(
                reservation.getId(),
                FindMemberOfWaitingResponse.from(reservation.getMember()),
                reservation.getDate(),
                FindTimeOfWaitingResponse.from(reservation.getReservationTime()),
                FindThemeOfWaitingResponse.from(reservation.getTheme())
        );
    }
}
