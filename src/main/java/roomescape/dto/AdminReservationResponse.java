package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.ReservationSlot;
import roomescape.domain.Theme;

public record AdminReservationResponse(
        Long id,
        LocalDate date,
        String themeName,

        @JsonFormat(pattern = "HH:mm")
        LocalTime time) {
    public static AdminReservationResponse from(ReservationSlot reservationSlot, Theme theme) {
        return new AdminReservationResponse(
                reservationSlot.getId(),
                reservationSlot.getDate(),
                theme.getName(),
                reservationSlot.getTime().getStartAt()
        );
    }
}
