package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.WaitingResult;

public record BookingResponse(
        Long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime time
) {

    public static BookingResponse from(ReservationResult result) {
        return new BookingResponse(
                result.id(),
                result.member().name(),
                result.theme().name(),
                result.date(),
                result.time().startAt()
        );
    }

    public static BookingResponse from(WaitingResult result) {
        return new BookingResponse(
                result.id(),
                result.waiter().name(),
                result.theme().name(),
                result.date(),
                result.time().startAt()
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
