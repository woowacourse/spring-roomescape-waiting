package roomescape.payment.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.NotFoundException;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentAmountMismatchException;
import roomescape.payment.exception.PaymentConfirmationUncertainException;
import roomescape.payment.exception.PaymentConnectionFailedException;
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

        PaymentResult result = requestConfirmation(payment, paymentKey, orderId, amount);
        Payment confirmed = payment.confirm(paymentKey, LocalDateTime.now());
        paymentRepository.save(confirmed);

        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new NotFoundException(PaymentErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.save(reservation.confirm());

        return result;
    }

    @Transactional
    public PaymentResult retryConfirmation(String orderId) {
        Payment payment = findByOrderId(orderId);
        if (payment.getPaymentKey() == null) {
            throw new BadRequestException("재시도할 결제 시도 내역이 없습니다.");
        }
        return confirm(payment.getPaymentKey(), orderId, payment.getAmount());
    }

    private PaymentResult requestConfirmation(Payment payment, String paymentKey, String orderId, Long amount) {
        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount, payment.getIdempotencyKey());
        try {
            return paymentGateway.confirm(confirmation);
        } catch (ResourceAccessException e) {
            // 연결 단계 실패(거부/타임아웃) — 토스가 요청을 받지도 못했으므로 확정 실패로 취급한다.
            throw new PaymentConnectionFailedException();
        } catch (RestClientException e) {
            // 응답 읽기 단계 실패(read timeout) — 토스가 이미 승인했을 수 있어 실패로 단정하지 않는다.
            paymentRepository.save(payment.markUncertain(paymentKey, LocalDateTime.now()));
            throw new PaymentConfirmationUncertainException(orderId);
        }
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
