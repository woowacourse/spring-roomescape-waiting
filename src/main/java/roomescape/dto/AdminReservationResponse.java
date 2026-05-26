package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public record AdminReservationResponse(
        Long id,
        LocalDate date,
        String themeName,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time) {
    public static AdminReservationResponse from(Reservation reservation, Theme theme) {
        return new AdminReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                reservation.getTime().getStartAt()
        );
    }
}
