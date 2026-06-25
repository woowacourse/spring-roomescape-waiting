package roomescape.application;

import roomescape.domain.Payment;
import roomescape.domain.Reservation;

public record ReservationPayment(Reservation reservation, Payment payment) {
}
