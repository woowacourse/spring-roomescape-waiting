package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.model.Waiting;

public record FindWaitingResponse(
        Long id,
        FindMemberOfWaitingResponse member,
        LocalDate date,
        FindTimeOfWaitingResponse time,
        FindThemeOfWaitingResponse theme,
        boolean isFirst) {

    public static FindWaitingResponse from(final Waiting waiting, boolean isFirst) {
        return new FindWaitingResponse(
                waiting.getId(),
                FindMemberOfWaitingResponse.from(waiting.getMember()),
                waiting.getDate(),
                FindTimeOfWaitingResponse.from(waiting.getReservationTime()),
                FindThemeOfWaitingResponse.from(waiting.getTheme()),
                isFirst
        );
    }
}
