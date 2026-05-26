package roomescape.time.presentation.dto;

import java.util.List;
import lombok.Builder;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.time.application.dto.AvailableReservationTimeInfo;

@Builder
public record AvailableReservationTimeResponse(
        ThemeResponse theme,
        List<ReservationTimeResponse> times
) {
    public static AvailableReservationTimeResponse from(final AvailableReservationTimeInfo info) {
        return AvailableReservationTimeResponse.builder()
                .times(info.times().stream().map(ReservationTimeResponse::from).toList())
                .theme(ThemeResponse.from(info.theme()))
                .build();
    }
}
