package roomescape.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.payment.Payment;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.payment.dao.PaymentDao;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.dao.ReservationDao;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private ReservationDao reservationDao;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 결제가_승인되면_예약을_확정한다() {
        Long reservationId = 1L;
        String paymentKey = "payment-key";
        String orderId = "reservation-1-order";
        Long amount = 50_000L;
        Payment payment = new Payment(1L, reservationId, null, orderId, PaymentStatus.READY, amount);
        PaymentResult paymentResult = new PaymentResult(paymentKey, orderId, PaymentStatus.DONE, amount);

        given(paymentDao.selectByOrderId(orderId)).willReturn(payment);
        given(paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, amount))).willReturn(paymentResult);

        PaymentResult actual = paymentService.confirm(paymentKey, orderId, amount);

        assertThat(actual).isEqualTo(paymentResult);
        verify(paymentDao).updateApproved(orderId, paymentKey, PaymentStatus.DONE);
        verify(reservationDao).updateStatusById(reservationId, ReservationStatus.CONFIRMED);
    }
}
