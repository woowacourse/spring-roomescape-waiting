package roomescape.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.dao.PaymentDao;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.port.PaymentGateway;

@Service
public class PaymentService {

  private final PaymentGateway paymentGateway;
  private final PaymentDao paymentDao;

  public PaymentService(PaymentGateway paymentGateway, PaymentDao paymentDao) {
    this.paymentGateway = paymentGateway;
    this.paymentDao = paymentDao;
  }

  public void approve(Long reservationId, PaymentConfirmation paymentConfirmation) {
    paymentGateway.confirm(paymentConfirmation);

    int updated = paymentDao.updateStatusIfUnknown(reservationId, PaymentStatus.DONE);
    if (updated == 0) {
      paymentDao.insert(reservationId, paymentConfirmation.paymentKey(),
          paymentConfirmation.orderId(), paymentConfirmation.amount(), PaymentStatus.DONE);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveUnknown(Long reservationId, String paymentKey, String orderId, Long amount) {
    paymentDao.insert(reservationId, paymentKey, orderId, amount, PaymentStatus.UNKNOWN);
  }
}
