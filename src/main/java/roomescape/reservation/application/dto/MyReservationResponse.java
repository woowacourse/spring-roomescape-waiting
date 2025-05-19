package roomescape.reservation.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(
        Long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {

    public static MyReservationResponse from(Reservation reservation, Long count) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                getBookingStatusFormat(reservation.getBookingStatus(), count)
        );
    }

    private static String getBookingStatusFormat(final BookingStatus bookingStatus, final Long count) {
        if (bookingStatus == BookingStatus.RESERVED) {
            return bookingStatus.getValue();
        }
        return count.toString() + "번째 예약" + bookingStatus.getValue();
    }

}
