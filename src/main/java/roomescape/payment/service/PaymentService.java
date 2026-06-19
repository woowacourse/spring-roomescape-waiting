package roomescape.payment.service;

import org.springframework.stereotype.Service;
import roomescape.payment.client.TossPaymentClient;
import roomescape.payment.dao.PaymentDao;
import roomescape.payment.dto.request.ConfirmRequest;

@Service
public class PaymentService {

  private final TossPaymentClient tossPaymentClient;
  private final PaymentDao paymentDao;

  public PaymentService(TossPaymentClient tossPaymentClient, PaymentDao paymentDao) {
    this.tossPaymentClient = tossPaymentClient;
    this.paymentDao = paymentDao;
  }

  public void approve(Long reservationId, ConfirmRequest confirmRequest) {
    tossPaymentClient.confirm(confirmRequest.paymentKey(), confirmRequest.orderId(),
        confirmRequest.amount());
    paymentDao.insert(reservationId, confirmRequest.paymentKey(), confirmRequest.orderId(),
        confirmRequest.amount());
  }
}
