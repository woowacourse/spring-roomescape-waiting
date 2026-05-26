package roomescape.reservation.presentation.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.time.presentation.dto.ReservationTimeResponse;

@Builder
public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse from(final ReservationInfo reservation) {
        return ReservationResponse.builder()
                .id(reservation.id())
                .name(reservation.name())
                .date(reservation.date())
                .time(ReservationTimeResponse.from(reservation.time()))
                .theme(ThemeResponse.from(reservation.theme()))
                .build();
    }
}
