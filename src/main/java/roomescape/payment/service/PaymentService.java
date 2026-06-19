package roomescape.payment.service;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.client.PaymentTimeoutException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentAmountMismatchException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentResultPersister resultPersister;

    public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository,
                          PaymentGateway paymentGateway, PaymentResultPersister resultPersister) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
        this.resultPersister = resultPersister;
    }

    @Transactional
    public String prepare(Long reservationId, Long amount) {
        String orderId = generateOrderId();
        String idempotencyKey = UUID.randomUUID().toString();
        paymentRepository.save(Payment.pending(reservationId, orderId, amount, idempotencyKey));
        return orderId;
    }

    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.getByOrderId(orderId);
        if (payment.isAmountMismatched(amount)) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }
        try {
            PaymentResult result = paymentGateway.confirm(
                    new PaymentConfirmation(paymentKey, orderId, amount, payment.getIdempotencyKey()));
            resultPersister.persistConfirmed(payment.getReservationId(), orderId, result.paymentKey());
            return result;
        } catch (PaymentTimeoutException e) {
            resultPersister.persistUnknown(orderId, paymentKey);
            throw e;
        }
    }

    public Optional<Payment> findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId);
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