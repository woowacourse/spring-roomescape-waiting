package roomescape.holiday.controller.dto;

import roomescape.holiday.domain.Holiday;

import java.time.LocalDate;

public record HolidayResponse(Long id, LocalDate date) {

    public static HolidayResponse from(Holiday holiday) {
        return new HolidayResponse(
                holiday.getId(),
                holiday.getDate()
        );
    }
}
