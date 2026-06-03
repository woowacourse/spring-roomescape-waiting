package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.application.dto.ReservationWaitingInfo;
import roomescape.reservation.domain.Status;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.time.presentation.dto.ReservationTimeResponse;

@Builder
public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Status status,
        Long waitingOrder

) {
    public static ReservationWaitingResponse from(ReservationWaitingInfo reservation) {
        return ReservationWaitingResponse.builder()
                .id(reservation.id())
                .name(reservation.name())
                .date(reservation.date())
                .time(ReservationTimeResponse.from(reservation.time()))
                .theme(ThemeResponse.from(reservation.theme()))
                .status(reservation.status())
                .waitingOrder(reservation.waitingOrder())
                .build();
    }
}
