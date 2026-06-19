package roomescape.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final TransactionTemplate transactionTemplate;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            ReservationRepository reservationRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 결제 전 주문을 준비한다. 같은 예약에 아직 미승인(payment_key=null) 주문이 남아 있으면
     * 결제창을 새로고침할 때마다 주문이 늘어나지 않도록 그 주문을 재사용한다.
     */
    @Transactional
    public Payment createOrder(Long reservationId, Long amount) {
        return paymentRepository.findByReservationId(reservationId)
                .filter(existing -> existing.paymentKey() == null)
                .orElseGet(() -> paymentRepository.save(
                        new Payment(null, reservationId, generateOrderId(), amount, null)));
    }

    /**
     * 외부 승인 호출은 트랜잭션 밖에서 수행해, 네트워크 대기 동안 DB 커넥션을 점유하지 않는다.
     * 저장 금액으로 먼저 검증해 위변조 amount 를 게이트웨이 호출 전에 차단하고,
     * 승인 성공 뒤의 DB 반영(payment_key 저장 + 예약 확정)만 하나의 짧은 트랜잭션으로 묶어 원자화한다.
     */
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다: orderId=" + orderId));
        if (!payment.amount().equals(amount)) {
            throw new PaymentAmountMismatchException(payment.amount(), amount);
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        transactionTemplate.executeWithoutResult(status -> {
            paymentRepository.updatePaymentKey(orderId, result.paymentKey());
            reservationRepository.confirm(payment.reservationId());
        });
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

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }
}
