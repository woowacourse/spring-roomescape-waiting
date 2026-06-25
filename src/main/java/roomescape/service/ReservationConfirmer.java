package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationResult;

@Service
public class ReservationConfirmer {

    private final AdminReservationService reservationService;
    private final PaymentOrderRepository paymentOrderRepository;

    public ReservationConfirmer(
            AdminReservationService reservationService,
            PaymentOrderRepository paymentOrderRepository
    ) {
        this.reservationService = reservationService;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    @Transactional
    public ReservationResult confirmReservation(String orderId, String paymentKey) {
        PaymentOrder order = paymentOrderRepository.getByOrderId(orderId);
        ReservationCreateCommand command = new ReservationCreateCommand(
                order.getReserverName(), order.getDate(), order.getTimeId(), order.getThemeId());
        ReservationResult reservation = reservationService.create(command);
        paymentOrderRepository.markConfirmed(orderId, paymentKey, reservation.id());
        return reservation;
    }
}
