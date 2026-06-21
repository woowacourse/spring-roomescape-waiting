package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.ReservationSlot;

public record AdminReservationResponse(
        long id,
        LocalDate date,
        String themeName,

        @JsonFormat(pattern = "HH:mm")
        LocalTime time) {
    public static AdminReservationResponse from(ReservationSlot slot) {
        return new AdminReservationResponse(
                slot.getId(),
                slot.getDate(),
                slot.getTheme().getName(),
                slot.getTime().getStartAt()
        );
    }
}
