package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.dto.payment.PaymentConfirmRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.PaymentException.AlreadyProcessedException;

@Service
public class PaymentService {

    private final TransactionTemplate transactionTemplate;

    private final ReservationOrderService reservationOrderService;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(TransactionTemplate transactionTemplate,
                          ReservationOrderService reservationOrderService,
                          ReservationRepository reservationRepository,
                          PaymentGateway paymentGateway) {
        this.transactionTemplate = transactionTemplate;
        this.reservationOrderService = reservationOrderService;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    public ReservationResponse confirm(PaymentConfirmRequest request) {

        ReservationOrder order = reservationOrderService.getByOrderId(request.orderId());
        order.verifyAmount(request.amount());
        verifyReservationPayable(order.getReservationId());

        PaymentResult result = paymentGateway.confirm(request.paymentKey(), request.orderId(), request.amount());

        try {
            return transactionTemplate.execute(status -> ReservationResponse.from(complete(order, result.paymentKey())));
        } catch (RuntimeException e) {
            /* refund */
            throw e;
        }
    }


    private void verifyReservationPayable(long reservationId) {
        Reservation reservation = reservationRepository.findReservationById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("예약이 존재하지 않습니다."));
        if (reservation.isPaid()) {
            throw new AlreadyProcessedException("이미 결제된 예약입니다.");
        }
    }

    private Reservation complete(ReservationOrder order, String paymentKey) {
        reservationOrderService.completeOrder(order, paymentKey);

        Reservation reservation = reservationRepository.findReservationById(order.getReservationId())
                .orElseThrow(() -> new ResourceNotFoundException("예약을 찾을 수 없습니다."));
        Reservation confirmed = reservation.confirmPayment();

        int updated = reservationRepository.updatePaid(confirmed.getId(), confirmed.isPaid());
        if (updated == 0) {
            throw new ResourceNotFoundException("예약이 정리되어 결제를 반영할 수 없습니다.");
        }

        return confirmed;
    }
}
