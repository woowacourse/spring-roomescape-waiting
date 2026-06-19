package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import roomescape.payment.PaymentConfirmedEvent;
import roomescape.payment.PaymentFailedEvent;
import roomescape.repository.ReservationDao;

@Component
@RequiredArgsConstructor
public class ReservationPaymentEventHandler {

    private final ReservationDao reservationDao;

    @EventListener
    public void confirmReservation(PaymentConfirmedEvent event) {
        reservationDao.confirm(event.reservationId());
    }

    @EventListener
    public void deleteFailedPendingReservation(PaymentFailedEvent event) {
        reservationDao.deletePendingPaymentById(event.reservationId());
    }
}
