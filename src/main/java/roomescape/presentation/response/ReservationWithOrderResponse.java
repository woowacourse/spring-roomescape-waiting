package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.ReservationWithOrder;

public record ReservationWithOrderResponse(
    int order,
    long id,
    UserResponse user,
    LocalDate date,
    TimeSlotResponse time,
    ThemeResponse theme
) {

    public static ReservationWithOrderResponse from(final ReservationWithOrder reservationWithOrder) {
        var reservation = reservationWithOrder.reservation();
        return new ReservationWithOrderResponse(
            reservationWithOrder.order(),
            reservation.id(),
            roomescape.presentation.response.UserResponse.from(reservation.user()),
            reservation.slot().date(),
            TimeSlotResponse.from(reservation.slot().timeSlot()),
            ThemeResponse.from(reservation.slot().theme())
        );
    }

    public static List<ReservationWithOrderResponse> from(final List<ReservationWithOrder> reservationWithOrders) {
        return reservationWithOrders.stream()
            .map(ReservationWithOrderResponse::from)
            .toList();
    }
}
