package roomescape.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import roomescape.repository.PaymentOrderDao;

@Service
public class PaymentOrderStatusService {

    private final PaymentOrderDao paymentOrderDao;

    public PaymentOrderStatusService(PaymentOrderDao paymentOrderDao) {
        this.paymentOrderDao = paymentOrderDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markConfirmUnknown(String orderId, String code, String message) {
        paymentOrderDao.markConfirmUnknown(orderId, code, message, LocalDateTime.now());
    }
}
