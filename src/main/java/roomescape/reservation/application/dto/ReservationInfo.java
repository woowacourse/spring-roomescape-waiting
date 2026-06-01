package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.PendingReservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;

@Builder
public record ReservationInfo(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        Status status
) {
    public static ReservationInfo from(final ActiveReservation reservation) {
        return ReservationInfo.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .date(reservation.getSlot().getDate())
                .time(ReservationTimeInfo.from(reservation.getSlot().getTime()))
                .theme(ThemeInfo.from(reservation.getSlot().getTheme()))
                .status(Status.ACTIVE)
                .build();
    }

    public static ReservationInfo from(final PendingReservation reservation) {
        return ReservationInfo.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .date(reservation.getSlot().getDate())
                .time(ReservationTimeInfo.from(reservation.getSlot().getTime()))
                .theme(ThemeInfo.from(reservation.getSlot().getTheme()))
                .status(Status.PENDING)
                .build();
    }
}
