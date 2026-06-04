package roomescape.reservation.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.theme.application.dto.ThemeInfo;
import roomescape.time.application.dto.ReservationTimeInfo;

@Builder
public record ReservationWaitingInfo(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeInfo time,
        ThemeInfo theme,
        Status status,
        Long waitingOrder
) {
    public static ReservationWaitingInfo from(Reservation reservation, Long waitingOrder) {
        return ReservationWaitingInfo.builder()
                .id(reservation.getId())
                .name(reservation.getName())
                .date(reservation.getDate())
                .time(ReservationTimeInfo.from(reservation.getTime()))
                .theme(ThemeInfo.from(reservation.getTheme()))
                .status(reservation.getStatus())
                .waitingOrder(waitingOrder)
                .build();
    }
}
