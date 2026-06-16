package roomescape.payment.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentAmountMismatchException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository,
                          PaymentGateway paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public String prepare(Long reservationId, Long amount) {
        String orderId = generateOrderId();
        paymentRepository.save(Payment.pending(reservationId, orderId, amount));
        return orderId;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.getByOrderId(orderId);
        if (payment.isAmountMismatched(amount)) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }
        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        paymentRepository.confirm(orderId, result.paymentKey());
        reservationRepository.confirm(payment.getReservationId());
        return result;
    }

    public String getPendingOrderId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .filter(Payment::isPending)
                .map(Payment::getOrderId)
                .orElse(null);
    }

    @Transactional
    public void cancelPending(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }
        Payment payment = paymentRepository.getByOrderId(orderId);
        if (!payment.isPending()) {
            return;
        }
        paymentRepository.deleteByOrderId(orderId);
        reservationRepository.deleteById(payment.getReservationId());
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }
}