package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;

@Builder
public record ReservationInfo(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme
) {
    public static ReservationInfo from(final Reservation reservation) {
        return ReservationInfo.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .date(reservation.getDate())
                .time(ReservationTimeInfo.from(reservation.getTime()))
                .theme(ThemeInfo.from(reservation.getTheme()))
                .build();
    }
}
