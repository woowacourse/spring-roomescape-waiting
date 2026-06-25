package roomescape.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Payment;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.exception.NotFoundException;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.repository.PaymentRepository;
import roomescape.service.PaymentService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceMockTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void confirm은_금액이_일치하면_게이트웨이에_승인을_요청하고_paymentKey를_저장한다() {
        Payment order = new Payment(1L, "order-1", 50_000L, null, 10L);
        given(paymentRepository.findByOrderId("order-1")).willReturn(Optional.of(order));

        Payment result = paymentService.confirm("pk_1", "order-1", 50_000L);

        verify(paymentGateway).confirm(new PaymentConfirmation("pk_1", "order-1", 50_000L));
        verify(paymentRepository).updatePaymentKey("order-1", "pk_1");
        assertThat(result.getPaymentKey()).isEqualTo("pk_1");
    }

    @Test
    void confirm은_금액이_다르면_게이트웨이를_호출하지_않고_예외를_던진다() {
        Payment order = new Payment(1L, "order-1", 50_000L, null, 10L);
        given(paymentRepository.findByOrderId("order-1")).willReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.confirm("pk_1", "order-1", 999L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
        verify(paymentRepository, never()).updatePaymentKey(anyString(), anyString());
    }

    @Test
    void confirm은_주문이_없으면_게이트웨이를_호출하지_않고_예외를_던진다() {
        given(paymentRepository.findByOrderId("order-x")).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.confirm("pk_1", "order-x", 50_000L))
                .isInstanceOf(NotFoundException.class);

        verify(paymentGateway, never()).confirm(any());
    }
}
