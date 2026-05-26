package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.dto.ReservationQueryResult;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;

@Builder
public record ReservationPendingInfo(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        Status status,
        Long pendingOrder
) {
    public static ReservationPendingInfo from(final ReservationQueryResult result){
        return ReservationPendingInfo.builder()
                .id(result.id())
                .name(result.name())
                .date(result.date())
                .time(ReservationTimeInfo.from(result.time()))
                .theme(ThemeInfo.from(result.theme()))
                .status(result.status())
                .pendingOrder(result.pendingIndex())
                .build();
    }
}
