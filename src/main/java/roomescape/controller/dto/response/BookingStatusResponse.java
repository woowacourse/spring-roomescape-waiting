package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.service.dto.BookingStatus;
import roomescape.service.dto.BookingType;

public record BookingStatusResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme,
        BookingType bookingType,
        Long turn
) {

    public static BookingStatusResponse from(BookingStatus bookingStatus) {
        return new BookingStatusResponse(
                bookingStatus.id(),
                bookingStatus.name(),
                bookingStatus.date(),
                ReservationTimeResponse.from(bookingStatus.time()),
                ReservationThemeResponse.from(bookingStatus.theme()),
                bookingStatus.bookingType(),
                bookingStatus.turn()
        );
    }
}
