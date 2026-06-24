package roomescape.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.port.PaymentGateway;
import roomescape.payment.repository.PaymentRepository;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;

    public PaymentService(PaymentGateway paymentGateway, PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    public long getAmount(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return payment.getAmount();
    }

    @Transactional
    public void confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!amount.equals(payment.getAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        PaymentResult result = paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount));

        paymentRepository.updatePaymentKey(orderId, result.paymentKey());
        reservationRepository.updateStatus(payment.getReservationId(), PaymentStatus.CONFIRMED);
    }

    @Transactional
    public void markAsUncertain(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment ->
                reservationRepository.updateStatus(payment.getReservationId(), PaymentStatus.PAYMENT_UNCERTAIN));
    }

    @Transactional
    public void cancelPending(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment ->
                reservationRepository.findById(payment.getReservationId()).ifPresent(reservation -> {
                    // 결제 대기 상태일 때만 정리한다. 이미 CONFIRMED 된 예약은 건드리지 않는다(방어).
                    if (reservation.getStatus() == PaymentStatus.PAYMENT_PENDING) {
                        // payment 는 reservation FK 의 ON DELETE CASCADE 로 함께 삭제된다.
                        reservationRepository.deleteById(reservation.getId());
                    }
                }));
    }
}
