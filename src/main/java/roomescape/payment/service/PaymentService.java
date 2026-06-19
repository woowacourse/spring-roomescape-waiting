package roomescape.payment.service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.infrastructure.payment.toss.toss.TossPaymentException;
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
        Optional<Payment> existingPayment = paymentDao.selectByReservationId(reservationId);
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }
        String orderId = "reservation-" + reservationId + "-" + UUID.randomUUID().toString().replace("-", "");
        String idempotencyKey = UUID.randomUUID().toString();
        return paymentDao.save(new Payment(reservationId, orderId, idempotencyKey, RESERVATION_PAYMENT_AMOUNT));
    }

    public Optional<Payment> findOrderByReservationId(Long reservationId) {
        return paymentDao.selectByReservationId(reservationId);
    }

    public Payment findLatestOrderByReservationId(Long reservationId) {
        return paymentDao.selectByReservationId(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(noRollbackFor = {RoomescapeException.class, TossPaymentException.class})
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        Payment payment = paymentDao.selectByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getAmount().equals(amount)) {
            throw new PaymentAmountMismatchException(payment.getAmount(), amount);
        }
        var confirmation = new PaymentConfirmation(paymentKey, orderId, payment.getIdempotencyKey(), amount);
        PaymentResult result = confirmPayment(orderId, confirmation);
        if (result.status() != PaymentStatus.DONE) {
            paymentDao.updateStatus(orderId, PaymentStatus.FAILED);
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
        paymentDao.updateApproved(orderId, result.paymentKey(), result.status());
        reservationDao.updateStatusById(payment.getReservationId(), ReservationStatus.CONFIRMED);
        return result;
    }

    private PaymentResult confirmPayment(String orderId, PaymentConfirmation confirmation) {
        try {
            return paymentGateway.confirm(confirmation);
        } catch (TossPaymentException.AlreadyProcessed e) {
            paymentDao.updateStatus(orderId, PaymentStatus.UNKNOWN);
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_UNKNOWN);
        } catch (TossPaymentException e) {
            paymentDao.updateStatus(orderId, PaymentStatus.FAILED);
            throw e;
        } catch (RestClientException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                paymentDao.updateStatus(orderId, PaymentStatus.UNKNOWN);
                throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_UNKNOWN);
            }
            if (hasCause(e, ConnectException.class)) {
                paymentDao.updateStatus(orderId, PaymentStatus.FAILED);
                throw new RoomescapeException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE);
            }
            paymentDao.updateStatus(orderId, PaymentStatus.UNKNOWN);
            throw new RoomescapeException(ErrorCode.PAYMENT_CONFIRM_UNKNOWN);
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
