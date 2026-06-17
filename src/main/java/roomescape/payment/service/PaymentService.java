package roomescape.payment.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.exception.PaymentErrorCode;
import roomescape.payment.repository.PaymentRepository;
import roomescape.payment.service.dto.PaymentCheckoutInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentGateway paymentGateway;
    private final long defaultAmount;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationRepository reservationRepository,
            PaymentGateway paymentGateway,
            @Value("${payment.default-amount:50000}") long defaultAmount
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.paymentGateway = paymentGateway;
        this.defaultAmount = defaultAmount;
    }

    @Transactional
    public Payment issuePendingPayment(Reservation reservation) {
        String orderId = generateOrderId();
        Payment payment = Payment.pending(reservation.getId(), orderId, defaultAmount, LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public PaymentCheckoutInfo getCheckoutInfo(String orderId) {
        Payment payment = findByOrderId(orderId);
        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new NotFoundException(PaymentErrorCode.RESERVATION_NOT_FOUND));
        return new PaymentCheckoutInfo(reservation.getId(), reservation.getName(), payment.getOrderId(),
                payment.getAmount());
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = findByOrderId(orderId);
        if (!payment.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }

        if (payment.isConfirmed()) {
            return new PaymentResult(payment.getPaymentKey(), payment.getOrderId(),
                    roomescape.payment.domain.PaymentStatus.DONE, payment.getAmount());
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));
        Payment confirmed = payment.confirm(paymentKey, LocalDateTime.now());
        paymentRepository.save(confirmed);

        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new NotFoundException(PaymentErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.save(reservation.confirm());

        return result;
    }

    @Transactional
    public void deleteByReservationId(long reservationId) {
        paymentRepository.deleteByReservationId(reservationId);
    }

    @Transactional
    public void cleanupByOrderId(String orderId) {
        if (orderId == null) {
            return;
        }

        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            paymentRepository.deleteByOrderId(orderId);
            reservationRepository.findById(payment.getReservationId()).ifPresent(reservationRepository::delete);
        });
    }

    private Payment findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private String generateOrderId() {
        return "order_" + UUID.randomUUID().toString().replace("-", "");
    }
}
