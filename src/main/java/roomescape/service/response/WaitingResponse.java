package roomescape.service.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static WaitingResponse from(final Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName().name(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getReservationTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }

    public static List<WaitingResponse> from(final List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
