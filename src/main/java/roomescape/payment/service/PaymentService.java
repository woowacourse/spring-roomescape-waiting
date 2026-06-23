package roomescape.payment.service;

import org.springframework.stereotype.Service;
import roomescape.payment.dao.PaymentDao;
import roomescape.payment.domain.PaymentConfirmation;
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
    paymentDao.insert(reservationId, paymentConfirmation.paymentKey(),
        paymentConfirmation.orderId(), paymentConfirmation.amount());
  }
}
