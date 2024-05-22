package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;

public record FindWaitingResponse(
        Long id,
        FindMemberOfWaitingResponse member,
        LocalDate date,
        FindTimeOfWaitingResponse time,
        FindThemeOfWaitingResponse theme) {

    public static FindWaitingResponse from(final Waiting waiting) {
        return new FindWaitingResponse(
                waiting.getId(),
                FindMemberOfWaitingResponse.from(waiting.getMember()),
                waiting.getDate(),
                FindTimeOfWaitingResponse.from(waiting.getReservationTime()),
                FindThemeOfWaitingResponse.from(waiting.getTheme())
        );
    }
}
