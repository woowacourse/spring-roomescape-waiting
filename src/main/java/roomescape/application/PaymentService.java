package roomescape.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationStatus;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.repository.PaymentRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.client.PaymentAlreadyProcessedException;
import roomescape.exception.client.PaymentAmountMismatchException;
import roomescape.exception.client.PaymentRejectedException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;
    private final OrderIdGenerator orderIdGenerator;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway,
            ReservationRepository reservationRepository,
            OrderIdGenerator orderIdGenerator
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
        this.orderIdGenerator = orderIdGenerator;
    }

    /**
     * 결제 인증 전: 주문(orderId, amount)을 먼저 저장하고 orderId를 돌려준다(요구사항 1).
     */
    @Transactional
    public String prepare(Long reservationId, long amount) {
        String orderId = orderIdGenerator.generate();
        paymentRepository.save(Payment.pending(reservationId, orderId, amount));
        return orderId;
    }

    /**
     * successUrl 콜백: 금액 검증(요구사항 3) → 승인(요구사항 4) → paymentKey 저장 + 예약 확정. 금액이 다르면 게이트웨이를 호출하지 않고 차단한다.
     */
    @Transactional
    public void confirm(String paymentKey, String orderId, long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentRejectedException("존재하지 않는 주문입니다."));

        if (!payment.isPending()) {
            throw new PaymentAlreadyProcessedException("이미 처리된 결제입니다.");
        }

        if (payment.getAmount() != amount) {
            log.warn("결제 금액 불일치 차단: orderId={}, 저장={}, 요청={}",
                    orderId, payment.getAmount(), amount);
            throw new PaymentAmountMismatchException("결제 금액이 주문 정보와 일치하지 않습니다.");
        }

        PaymentResult result = paymentGateway.confirm(
                new PaymentConfirmation(paymentKey, orderId, amount));

        Payment confirmed = payment.confirm(result.paymentKey(), result.status()); // PENDING 불변식 가드
        paymentRepository.updateConfirmed(
                confirmed.getOrderId(), confirmed.getPaymentKey(), confirmed.getStatus());
        reservationRepository.updateStatus(confirmed.getReservationId(), ReservationStatus.CONFIRMED);
    }

    /**
     * 결제 대기 상태의 주문/예약을 정리한다(요구사항 7). 이미 확정된 결제는 건드리지 않는다.
     */
    @Transactional
    public void cancelPending(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (!payment.isPending()) {
                return;
            }
            Long reservationId = payment.getReservationId();
            paymentRepository.deleteByReservationId(reservationId); // FK: 결제 먼저
            reservationRepository.deleteById(reservationId);
        });
    }


}
