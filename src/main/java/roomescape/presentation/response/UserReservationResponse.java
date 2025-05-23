package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.ReservationWithOrder;

public record UserReservationResponse(
    long id,
    LocalDate date,
    TimeSlotResponse time,
    ThemeResponse theme,
    String status
) {

    public static UserReservationResponse from(final ReservationWithOrder waiting) {
        var reservation = waiting.reservation();
        return new UserReservationResponse(
            reservation.id(),
            reservation.slot().date(),
            TimeSlotResponse.from(reservation.slot().timeSlot()),
            ThemeResponse.from(reservation.slot().theme()),
            writeDescription(waiting)
        );
    }

    private static String writeDescription(final ReservationWithOrder waiting) {
        if (waiting.isWaiting()) {
            return waiting.order() + "번째 예약 대기";
        }
        var reservation = waiting.reservation();
        return reservation.status().description();
    }

    public static List<UserReservationResponse> from(final List<ReservationWithOrder> waitings) {
        return waitings.stream()
            .map(UserReservationResponse::from)
            .toList();
    }
}
