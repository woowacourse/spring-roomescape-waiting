package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.reservation.domain.Reservation;
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
        Status status,
        LocalDateTime createdAt
) {
    public static ReservationInfo from(final Reservation reservation) {
        return ReservationInfo.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .date(reservation.getDate())
                .time(ReservationTimeInfo.from(reservation.getTime()))
                .theme(ThemeInfo.from(reservation.getTheme()))
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
