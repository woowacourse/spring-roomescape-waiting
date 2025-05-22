package roomescape.controller.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.WaitingResult;

public record BookingResponse(
        Long id,
        String member,
        String theme,
        LocalDate date,
        LocalTime time
) {

    public static BookingResponse from(ReservationResult result) {
        return new BookingResponse(
                result.id(),
                result.memberName(),
                result.themeName(),
                result.date(),
                result.time()
        );
    }

    public static BookingResponse from(WaitingResult result) {
        return new BookingResponse(
                result.id(),
                result.waiterName(),
                result.themeName(),
                result.date(),
                result.time()
        );
    }

    public static List<BookingResponse> fromReservations(List<ReservationResult> reservationResults) {
        return reservationResults.stream()
                .map(BookingResponse::from)
                .toList();
    }

    public static List<BookingResponse> fromWaitings(List<WaitingResult> waitingResults) {
        return waitingResults.stream()
                .map(BookingResponse::from)
                .toList();
    }
}
