package roomescape.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.ReservationStatus;
import roomescape.repository.PaymentRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.exception.PaymentAmountMismatchException;
import roomescape.service.exception.ResourceNotFoundException;

/**
 * 결제 유스케이스. 게이트웨이 호출 '전에' 저장된 주문 금액으로 검증하고,
 * 승인 성공 시 payment_key 를 저장한 뒤 예약을 CONFIRMED 로 전이한다.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            ReservationRepository reservationRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Payment createOrder(Long reservationId, Long amount) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        return paymentRepository.save(new Payment(null, reservationId, orderId, amount, null));
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다: orderId=" + orderId));
        if (!payment.amount().equals(amount)) {
            throw new PaymentAmountMismatchException(payment.amount(), amount);
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        paymentRepository.updatePaymentKey(orderId, result.paymentKey());
        reservationRepository.confirm(payment.reservationId());
        return result;
    }

    /**
     * 결제 실패/취소 시 결제 대기(PENDING) 예약과 주문을 정리한다.
     * 이미 확정된 예약은 건드리지 않아 재생·위조 호출로부터 보호한다.
     */
    @Transactional
    public void cancelOrder(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment ->
                reservationRepository.findById(payment.reservationId())
                        .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING)
                        .ifPresent(reservation -> {
                            reservationRepository.deleteById(reservation.getId());
                            paymentRepository.deleteByOrderId(orderId);
                        }));
    }
}
