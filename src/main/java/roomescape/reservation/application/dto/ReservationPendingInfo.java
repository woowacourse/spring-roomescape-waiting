package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.ActiveReservation;
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
    public static ReservationPendingInfo from(final ActiveReservation reservation) {
        return ReservationPendingInfo.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .date(reservation.getSlot().getDate())
                .time(ReservationTimeInfo.from(reservation.getSlot().getTime()))
                .theme(ThemeInfo.from(reservation.getSlot().getTheme()))
                .status(Status.ACTIVE)
                .build();
    }

    public static ReservationPendingInfo from(final ReservationQueryResult result) {
        return ReservationPendingInfo.builder()
                .id(result.id())
                .name(result.name())
                .date(result.slot().getDate())
                .time(ReservationTimeInfo.from(result.slot().getTime()))
                .theme(ThemeInfo.from(result.slot().getTheme()))
                .status(Status.PENDING)
                .pendingOrder(result.pendingIndex())
                .build();
    }
}
