package roomescape.payment.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.payment.Payment;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.payment.dao.PaymentDao;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dao.ReservationDao;

/**
 * 결제 승인 유스케이스. 게이트웨이 호출 '전에' 금액을 검증하는 것이 핵심이다.
 */
@Service
public class PaymentService {
    private static final long RESERVATION_PAYMENT_AMOUNT = 50_000L;

    private final PaymentDao paymentDao;
    private final PaymentGateway paymentGateway;
    private final ReservationDao reservationDao;

    public PaymentService(PaymentDao paymentDao, PaymentGateway paymentGateway, ReservationDao reservationDao) {
        this.paymentDao = paymentDao;
        this.paymentGateway = paymentGateway;
        this.reservationDao = reservationDao;
    }

    public Payment createReservationOrder(Long reservationId) {
        String orderId = "reservation-" + reservationId + "-" + UUID.randomUUID().toString().replace("-", "");
        return paymentDao.save(new Payment(reservationId, orderId, RESERVATION_PAYMENT_AMOUNT));
    }

    public Payment findLatestOrderByReservationId(Long reservationId) {
        Payment payment = paymentDao.selectByReservationId(reservationId);
        if (payment == null) {
            throw new RoomescapeException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        var payment = paymentDao.selectByOrderId(orderId);
        if (payment == null) {
            throw new RoomescapeException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        if (!payment.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }
        var confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        PaymentResult result = paymentGateway.confirm(confirmation);
        if (result.status() != PaymentStatus.DONE) {
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        paymentDao.updateApproved(orderId, result.paymentKey(), result.status());
        reservationDao.updateStatusById(payment.getReservationId(), ReservationStatus.CONFIRMED);
        return result;
    }

}
