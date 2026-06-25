package roomescape.unit.api;

import org.junit.jupiter.api.Test;
import roomescape.api.PaymentController;
import roomescape.application.ReservationApplicationService;
import roomescape.dto.PaymentConfirmRequest;
import roomescape.exception.PaymentUncertainException;
import roomescape.payment.toss.TossPaymentException;
import roomescape.service.PaymentService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    private final ReservationApplicationService applicationService = mock(ReservationApplicationService.class);
    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentController paymentController = new PaymentController(applicationService, paymentService);

    @Test
    void 결제_결과가_불명확하면_주문을_확인필요로_표시하고_예외를_전파한다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("pk_1", 50_000L);
        when(applicationService.confirmReservation(eq("order-1"), any()))
                .thenThrow(new PaymentUncertainException("결과 불명확", new RuntimeException()));

        assertThatThrownBy(() -> paymentController.confirm("order-1", request))
                .isInstanceOf(PaymentUncertainException.class);
        verify(paymentService).markUncertain("order-1");
    }

    @Test
    void 토스가_결제를_거절하면_주문을_실패로_표시하고_예외를_전파한다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("pk_1", 50_000L);
        when(applicationService.confirmReservation(eq("order-1"), any()))
                .thenThrow(new TossPaymentException("거절"));

        assertThatThrownBy(() -> paymentController.confirm("order-1", request))
                .isInstanceOf(TossPaymentException.class);
        verify(paymentService).markFailed("order-1");
    }
}
