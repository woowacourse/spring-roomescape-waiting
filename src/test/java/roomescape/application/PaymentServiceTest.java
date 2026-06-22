package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationStatus;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.domain.repository.PaymentRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.client.PaymentAmountMismatchException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private OrderIdGenerator orderIdGenerator;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 저장금액과_다른_amount면_확인전에_차단되고_게이트웨이는_호출되지_않는다() {
        given(paymentRepository.findByOrderId("order_1"))
                .willReturn(Optional.of(Payment.pending(1L, "order_1", 1000L)));

        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order_1", 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 금액이_일치하면_승인하고_예약을_확정한다() {
        given(paymentRepository.findByOrderId("order_1"))
                .willReturn(Optional.of(Payment.pending(1L, "order_1", 1000L)));
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk_1", "order_1", PaymentStatus.DONE, 1000L));

        paymentService.confirm("test_pk_1", "order_1", 1000L);

        verify(paymentRepository).updateConfirmed("order_1", "test_pk_1", PaymentStatus.DONE);
        verify(reservationRepository).updateStatus(1L, ReservationStatus.CONFIRMED);
    }

    @Test
    void markInDoubt는_대기중_결제를_확인필요로_바꾼다() {
        given(paymentRepository.findByOrderId("order_1"))
                .willReturn(Optional.of(Payment.pending(1L, "order_1", 1000L)));

        paymentService.markInDoubt("order_1");

        verify(paymentRepository).updateStatus("order_1", PaymentStatus.IN_DOUBT);
    }

    @Test
    void markInDoubt는_이미_확정된_결제는_건드리지_않는다() {
        Payment done = Payment.withId(1L, 1L, "order_1", 1000L, "pk", PaymentStatus.DONE);
        given(paymentRepository.findByOrderId("order_1")).willReturn(Optional.of(done));

        paymentService.markInDoubt("order_1");

        verify(paymentRepository, never()).updateStatus(anyString(), any());
    }

}
