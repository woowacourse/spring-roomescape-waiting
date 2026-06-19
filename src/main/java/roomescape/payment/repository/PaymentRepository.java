package roomescape.payment.repository;

import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.payment.domain.Payment;
import roomescape.payment.exception.PaymentErrorCode;

@Repository
public class PaymentRepository {

    private final PaymentDao paymentDao;

    public PaymentRepository(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    public Payment save(Payment payment) {
        try {
            if (payment.getId() == null) {
                return paymentDao.save(payment);
            }
            paymentDao.update(payment);
            return payment;
        } catch (DuplicateKeyException e) {
            throw new ConflictException(PaymentErrorCode.DUPLICATE_PAYMENT);
        }
    }

    public Optional<Payment> findByOrderId(String orderId) {
        return paymentDao.findByOrderId(orderId);
    }

    public Optional<Payment> findByReservationId(long reservationId) {
        return paymentDao.findByReservationId(reservationId);
    }

    public void deleteByOrderId(String orderId) {
        paymentDao.deleteByOrderId(orderId);
    }

    public void deleteByReservationId(long reservationId) {
        paymentDao.deleteByReservationId(reservationId);
    }

    public void confirm(Payment payment, String paymentKey) {
        save(payment.confirm(paymentKey, payment.getUpdatedAt()));
    }

    public void cancel(Payment payment) {
        save(payment.cancel(payment.getUpdatedAt()));
    }
}
