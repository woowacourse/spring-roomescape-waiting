package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;
import roomescape.domain.Time;

public record ReservationResponse(
        Long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time) {
    public static ReservationResponse from(Reservation reservation) {
        Theme theme = reservation.getTheme();
        Time time = reservation.getTime();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                time.getStartAt()
        );
    }
}