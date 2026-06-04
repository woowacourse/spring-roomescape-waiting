package roomescape.time.application.dto;

import java.util.List;
import lombok.Builder;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Builder
public record AvailableReservationTimeInfo(
        ThemeInfo theme,
        List<ReservationTimeInfo> times
) {
    public static AvailableReservationTimeInfo from(Theme theme, List<ReservationTime> times) {
        return AvailableReservationTimeInfo.builder()
                .theme(ThemeInfo.from(theme))
                .times(times.stream().map(ReservationTimeInfo::from).toList())
                .build();
    }
}
