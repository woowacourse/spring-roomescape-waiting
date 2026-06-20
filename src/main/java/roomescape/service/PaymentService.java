package roomescape.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.PaymentOrderDao;
import roomescape.dao.ReservationDao;
import roomescape.domain.PaymentOrder;
import roomescape.domain.ReservationStatus;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderDao paymentOrderDao;
    private final ReservationDao reservationDao;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentOrderDao paymentOrderDao, ReservationDao reservationDao,
                          PaymentGateway paymentGateway) {
        this.paymentOrderDao = paymentOrderDao;
        this.reservationDao = reservationDao;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public void confirm(String paymentKey, String orderId, long amount) {
        PaymentOrder paymentOrder = getPaymentOrder(orderId);
        validateAmount(paymentOrder, amount);

        PaymentResult result = paymentGateway.confirm(
                new PaymentConfirmation(paymentKey, orderId, amount)
        );

        paymentOrderDao.confirm(paymentOrder.confirm(result.paymentKey()));
        reservationDao.updateStatus(paymentOrder.getReservationId(), ReservationStatus.CONFIRMED);

        log.info("결제 승인 완료: orderId={}, reservationId={}, paymentKey={}",
                orderId, paymentOrder.getReservationId(), result.paymentKey());
    }

    private PaymentOrder getPaymentOrder(String orderId) {
        return paymentOrderDao.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.NOT_FOUND_PAYMENT));
    }

    @Transactional
    public void cancel(String orderId) {
        paymentOrderDao.findByOrderId(orderId).ifPresent(paymentOrder -> {
            long reservationId = paymentOrder.getReservationId();
            paymentOrderDao.deleteByReservationId(reservationId);
            reservationDao.delete(reservationId);
            log.info("결제 실패로 인한 예약 취소: orderId={}, reservationId={}", orderId, reservationId);
        });
    }

    private void validateAmount(PaymentOrder paymentOrder, long requestedAmount) {
        if (paymentOrder.getAmount() != requestedAmount) {
            log.warn("결제 금액 불일치: orderId={}, stored={}, requested={}",
                    paymentOrder.getOrderId(), paymentOrder.getAmount(), requestedAmount);
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}
