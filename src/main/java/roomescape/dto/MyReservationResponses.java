package roomescape.dto;

import roomescape.domain.Payment;
import roomescape.domain.Reservation;

import java.util.List;
import java.util.Map;

public record MyReservationResponses(
        List<MyReservationResponse> reservations,
        long totalCount,
        int page,
        int size
) {
    public static MyReservationResponses from(
            List<Reservation> reservations,
            Map<Long, Payment> paymentsByReservationId,
            long totalCount,
            int page,
            int size
    ) {
        List<MyReservationResponse> items = reservations.stream()
                .map(reservation -> MyReservationResponse.of(
                        reservation, paymentsByReservationId.get(reservation.getId())))
                .toList();
        return new MyReservationResponses(items, totalCount, page, size);
    }
}
