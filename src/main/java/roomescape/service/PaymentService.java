package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.client.PaymentGatewayException;
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

    @Transactional(noRollbackFor = PaymentGatewayException.ReadTimeout.class)
    public PaymentResult confirm(String paymentKey, String orderId, Long amount) {
        PaymentOrder paymentOrder = paymentOrderDao.selectByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));

        if (!paymentOrder.getAmount().equals(amount)) {
            throw new RoomEscapeException(PaymentErrorCode.AMOUNT_MISMATCH);
        }

        PaymentResult result;
        try {
            PaymentConfirmation confirmation = new PaymentConfirmation(
                    paymentKey,
                    orderId,
                    amount,
                    paymentOrder.getIdempotencyKey()
            );
            result = paymentGateway.confirm(confirmation);
        } catch (PaymentGatewayException.ReadTimeout exception) {
            paymentOrderDao.update(paymentOrder.unknown());
            throw exception;
        }

        Reservation reservation = reservationDao.selectById(paymentOrder.getReservationId())
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.NOT_FOUND));

        Reservation confirmedReservation = reservation.confirmPayment(result.paymentKey());
        PaymentOrder confirmedPaymentOrder = paymentOrder.confirm();

        reservationDao.updatePayment(confirmedReservation);
        paymentOrderDao.update(confirmedPaymentOrder);

        return result;
    }

    @Transactional
    public void fail(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }

        PaymentOrder paymentOrder = paymentOrderDao.selectByOrderId(orderId)
                .orElseThrow(() -> new RoomEscapeException(PaymentErrorCode.ORDER_NOT_FOUND));

        paymentOrderDao.deleteByReservationId(paymentOrder.getReservationId());
        reservationDao.delete(paymentOrder.getReservationId());
    }
}
