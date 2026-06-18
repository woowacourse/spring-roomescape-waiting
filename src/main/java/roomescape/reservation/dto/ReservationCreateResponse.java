package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import roomescape.payment.domain.Order;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record ReservationCreateResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        @JsonInclude(Include.NON_NULL)
        PaymentInfo payment
) {

    public record PaymentInfo(String orderId, long amount) {
    }

    public static ReservationCreateResponse pending(Reservation reservation, Order order) {
        return of(reservation, new PaymentInfo(order.getOrderId().value(), order.getAmount()));
    }

    public static ReservationCreateResponse waiting(Reservation reservation) {
        return of(reservation, null);
    }

    private static ReservationCreateResponse of(Reservation reservation, PaymentInfo payment) {
        Slot slot = reservation.getSlot();
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ThemeResponse.from(slot.getTheme()),
                reservation.getStatus().name(),
                payment
        );
    }
}
