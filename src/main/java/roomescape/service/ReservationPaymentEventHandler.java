package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.payment.PaymentConfirmedEvent;
import roomescape.payment.PaymentFailedEvent;
import roomescape.repository.ReservationDao;

@Component
@RequiredArgsConstructor
public class ReservationPaymentEventHandler {

    private final ReservationDao reservationDao;
    private final WaitingCommandService waitingCommandService;

    @EventListener
    public void confirmReservation(PaymentConfirmedEvent event) {
        reservationDao.confirm(event.reservationId());
    }

    @EventListener
    public void deleteFailedPendingReservation(PaymentFailedEvent event) {
        reservationDao.findByIdForUpdate(event.reservationId())
                .filter(reservation -> reservation.status() == ReservationStatus.PENDING_PAYMENT)
                .ifPresent(this::deleteAndPromoteNextWaiting);
    }

    private void deleteAndPromoteNextWaiting(Reservation reservation) {
        reservationDao.deletePendingPaymentById(reservation.id());
        waitingCommandService.promoteNextWaitingIn(reservation.slot());
    }
}
