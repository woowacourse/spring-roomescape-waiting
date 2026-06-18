package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import roomescape.payment.domain.Order;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Slot;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;

public record UserReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        Long waitingNumber,
        @JsonInclude(Include.NON_NULL)
        PaymentInfo payment
) {

    public record PaymentInfo(String orderId, String paymentStatus, String paymentKey, long amount) {

        public static PaymentInfo from(Order order) {
            return new PaymentInfo(
                    order.getOrderId().value(),
                    order.getStatus().name(),
                    order.getPaymentKey(),
                    order.getAmount()
            );
        }
    }

    public static UserReservationResponse reserved(Reservation reservation, Order order) {
        Slot slot = reservation.getSlot();
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ThemeResponse.from(slot.getTheme()),
                reservation.getStatus().name(),
                null,
                order == null ? null : PaymentInfo.from(order)
        );
    }

    public static UserReservationResponse waiting(WaitingRank waitingRank) {
        Reservation reservation = waitingRank.reservation();
        Slot slot = reservation.getSlot();
        return new UserReservationResponse(
                reservation.getId(),
                reservation.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ThemeResponse.from(slot.getTheme()),
                reservation.getStatus().name(),
                waitingRank.waitingNumber(),
                null
        );
    }
}
