package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.common.exception.code.ReservationErrorCode;
import roomescape.dao.PaymentOrderDao;
import roomescape.dao.ReservationDao;
import roomescape.domain.*;

@Service
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentOrderDao paymentOrderDao;
    private final PaymentGateway paymentGateway;
    private final ReservationDao reservationDao;

    public PaymentService(PaymentOrderDao paymentOrderDao, PaymentGateway paymentGateway, ReservationDao reservationDao) {
        this.paymentOrderDao = paymentOrderDao;
        this.paymentGateway = paymentGateway;
        this.reservationDao = reservationDao;
    }

    @Transactional
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder paymentOrder = paymentOrderDao.selectByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!paymentOrder.getAmount().equals(amount)) {
            throw new RoomEscapeException(PaymentErrorCode.AMOUNT_MISMATCH);
        }

        PaymentConfirmation confirmation = new PaymentConfirmation(paymentKey, orderId, amount);
        PaymentResult result = paymentGateway.confirm(confirmation);

        Reservation reservation = reservationDao.selectById(paymentOrder.getReservationId())
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));

        Reservation confirmedReservation = reservation.confirmPayment(result.paymentKey());
        PaymentOrder confirmedPaymentOrder = paymentOrder.confirm();

        reservationDao.updatePayment(confirmedReservation);
        paymentOrderDao.update(confirmedPaymentOrder);

        return result;
    }
}
