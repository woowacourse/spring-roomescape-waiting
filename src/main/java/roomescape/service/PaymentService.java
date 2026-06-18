package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.PaymentOrderNotFoundException;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.payment.port.PaymentGateway;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentOrderRepository paymentOrderRepository,
                          ReservationRepository reservationRepository,
                          PaymentGateway paymentGateway) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentResult confirmPayment(String paymentKey, String orderId, long amount) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentOrderNotFoundException(orderId));

        validateAmount(amount, paymentOrder);

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount,
                paymentOrder.getIdempotencyKey()));
        paymentOrderRepository.updatePaymentKey(orderId, result.paymentKey());
        reservationRepository.updateStatus(paymentOrder.getReservationId(), ReservationStatus.RESERVED);
        return result;
    }

    @Transactional
    public void failPayment(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }

        paymentOrderRepository.findByOrderId(orderId)
                .ifPresent(this::deletePendingPaymentOrder);
    }

    private void deletePendingPaymentOrder(PaymentOrder paymentOrder) {
        reservationRepository.findById(paymentOrder.getReservationId())
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PAYMENT_PENDING)
                .filter(reservation -> paymentOrder.getPaymentKey() == null)
                .ifPresent(reservation -> {
                    paymentOrderRepository.deleteByOrderId(paymentOrder.getOrderId());
                    reservationRepository.deleteById(reservation.getId());
                });
    }

    private void validateAmount(long amount, PaymentOrder paymentOrder) {
        if (paymentOrder.getAmount() != amount) {
            throw new PaymentAmountMismatchException();
        }
    }
}
