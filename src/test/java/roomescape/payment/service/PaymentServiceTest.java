package roomescape.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.net.SocketTimeoutException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
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
        String idempotencyKey = "0f7dd2f2-cb41-4c97-a09f-f0b312944a53";
        Long amount = 50_000L;
        Payment payment = new Payment(1L, reservationId, null, orderId, idempotencyKey, PaymentStatus.READY, amount);
        PaymentResult paymentResult = new PaymentResult(paymentKey, orderId, PaymentStatus.DONE, amount);

        given(paymentDao.selectByOrderId(orderId)).willReturn(Optional.of(payment));
        given(paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, idempotencyKey, amount)))
                .willReturn(paymentResult);

        PaymentResult actual = paymentService.confirm(paymentKey, orderId, amount);

        assertThat(actual).isEqualTo(paymentResult);
        verify(paymentDao).updateApproved(orderId, paymentKey, PaymentStatus.DONE);
        verify(reservationDao).updateStatusById(reservationId, ReservationStatus.CONFIRMED);
    }

    @Test
    void 결제_승인_응답을_읽다가_타임아웃되면_확인_필요_상태로_저장한다() {
        Long reservationId = 1L;
        String paymentKey = "payment-key";
        String orderId = "reservation-1-order";
        String idempotencyKey = "0f7dd2f2-cb41-4c97-a09f-f0b312944a53";
        Long amount = 50_000L;
        Payment payment = new Payment(1L, reservationId, null, orderId, idempotencyKey, PaymentStatus.READY, amount);

        given(paymentDao.selectByOrderId(orderId)).willReturn(Optional.of(payment));
        given(paymentGateway.confirm(new PaymentConfirmation(paymentKey, orderId, idempotencyKey, amount)))
                .willThrow(new RestClientException("read timeout", new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> paymentService.confirm(paymentKey, orderId, amount))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_CONFIRM_UNKNOWN);

        verify(paymentDao).updateStatus(orderId, PaymentStatus.UNKNOWN);
    }
}
