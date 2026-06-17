package payment;

import java.time.LocalDateTime;

public record ReservationPendingPaymentEvent(long reservationId, LocalDateTime createdAt) {
}
