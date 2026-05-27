package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;
import roomescape.domain.Time;

public record ReservationResponse(
        Long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        WaitingResponse waitingResponse) {
    public static ReservationResponse from(ReservationSlot reservationSlot, WaitingResponse waitingResponse) {
        Theme theme = reservationSlot.getTheme();
        Time time = reservationSlot.getTime();
        return new ReservationResponse(
                reservationSlot.getId(),
                reservationSlot.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                time.getStartAt(),
                waitingResponse
        );
    }
}