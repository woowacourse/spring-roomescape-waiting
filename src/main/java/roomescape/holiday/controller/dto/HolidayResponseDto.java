package roomescape.holiday.controller.dto;

import roomescape.holiday.domain.Holiday;

import java.time.LocalDate;

public record HolidayResponseDto(Long id, LocalDate date) {

    public static HolidayResponseDto from(Holiday holiday) {
        return new HolidayResponseDto(
                holiday.id(),
                holiday.date()
        );
    }
}
