package roomescape.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.vo.ReservationSlotInfo;

public record AdminReservationResponse(
        long id,
        LocalDate date,
        String themeName,

        @JsonFormat(pattern = "HH:mm")
        LocalTime time) {
    public static AdminReservationResponse from(ReservationSlotInfo slot) {
        Theme theme = slot.theme();
        Time time = slot.time();
        return new AdminReservationResponse(
                slot.slotId(),
                slot.date(),
                theme.getName(),
                time.getStartAt()
        );
    }
}
