package roomescape.wating.controller.dto.response;

import roomescape.theme.controller.dto.response.ThemeWithoutIdResponse;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.dto.WaitingWithRank;

import java.time.LocalDate;
import java.time.LocalTime;

public record WaitingResponse(
        long id,
        String customerName,
        String customerEmail,
        LocalDate date,
        LocalTime startAt,
        ThemeWithoutIdResponse theme,
        int rank
) {

    public static WaitingResponse of(final Waiting waiting, final int rank) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getCustomerName().name(),
                waiting.getCustomerEmail(),
                waiting.getReservationDate(),
                waiting.getTime().getStartAt(),
                ThemeWithoutIdResponse.from(waiting.getTheme()),
                rank
        );
    }

    public static WaitingResponse from(final WaitingWithRank waitingWithRank) {
        return of(waitingWithRank.waiting(), waitingWithRank.rank());
    }
}
