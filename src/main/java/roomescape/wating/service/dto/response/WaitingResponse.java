package roomescape.wating.service.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.theme.service.dto.response.ThemeWithoutIdResponse;
import roomescape.wating.domain.Waiting;

public record WaitingResponse(
    long id,
    String customerName,
    LocalDate date,
    LocalTime startAt,
    ThemeWithoutIdResponse theme,
    int rank
) {

    public static WaitingResponse of(final Waiting waiting, final int rank) {
        return new WaitingResponse(
            waiting.getId(),
            waiting.getCustomerName().name(),
            waiting.getReservationDate(),
            waiting.getTime().getStartAt(),
            ThemeWithoutIdResponse.from(waiting.getTheme()),
            rank
        );
    }
}
