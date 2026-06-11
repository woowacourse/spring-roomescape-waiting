package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.vo.ReservationSlotInfo;

public record ReservationResponse(
        long reservationId,
        String name,
        String status,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        int order) {
    public static ReservationResponse from(Reservation reservation, ReservationSlotInfo reservationSlotInfo, int order) {
        Theme theme = reservationSlotInfo.theme();
        Time time = reservationSlotInfo.time();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStatus().toString(),
                reservationSlotInfo.date(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                time.getStartAt(),
                order
        );
    }
}
