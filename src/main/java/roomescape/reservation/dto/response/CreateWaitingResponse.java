package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.response.CreateMemberOfReservationResponse;
import roomescape.member.dto.response.CreateMemberOfWaitingResponse;
import roomescape.member.dto.response.CreateThemeOfReservationResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;

public record CreateWaitingResponse(Long id, CreateMemberOfWaitingResponse member, LocalDate date,
                                    CreateTimeOfWaitingResponse time, CreateThemeOfWaitingResponse theme) {
    public static CreateWaitingResponse from(final Waiting waiting) {
        return new CreateWaitingResponse(
                waiting.getId(),
                CreateMemberOfWaitingResponse.from(waiting.getMember()),
                waiting.getDate(),
                CreateTimeOfWaitingResponse.from(waiting.getReservationTime()),
                CreateThemeOfWaitingResponse.from(waiting.getTheme())
        );
    }
}
