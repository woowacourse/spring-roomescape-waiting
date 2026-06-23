package roomescape.controller.dto;

import roomescape.service.dto.Booking;

import java.time.LocalDate;

public record BookingResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse timeResponse,
        ThemeResponse themeResponse,
        boolean isReserved,
        Integer waitingNumber,
        Long amount
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.id(),
                booking.name(),
                booking.date(),
                TimeResponse.from(booking.timeSlot()),
                ThemeResponse.from(booking.theme()),
                booking.isReserved(),
                booking.waitingNumber(),
                booking.amount()
        );
    }
}
