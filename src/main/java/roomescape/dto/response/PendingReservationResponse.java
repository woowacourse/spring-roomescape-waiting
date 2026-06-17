package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Slot;
import roomescape.service.PendingReservation;

public record PendingReservationResponse(
        Long reservationId,
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        TimeInfo time,
        ThemeInfo theme,
        String status,
        PaymentCheckoutResponse payment
) {

    public static PendingReservationResponse from(PendingReservation pendingReservation, String clientKey) {
        Reservation reservation = pendingReservation.reservation();
        Slot slot = reservation.slot();
        PaymentCheckoutResponse payment = new PaymentCheckoutResponse(
                clientKey,
                pendingReservation.orderId(),
                pendingReservation.amount(),
                pendingReservation.orderName()
        );

        return new PendingReservationResponse(
                reservation.id(),
                reservation.owner().name(),
                slot.date(),
                TimeInfo.from(slot.time()),
                ThemeInfo.from(slot.theme()),
                reservation.status().name(),
                payment
        );
    }
}
