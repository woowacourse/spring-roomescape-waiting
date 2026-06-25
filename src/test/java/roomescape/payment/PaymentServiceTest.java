package roomescape.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.payment.order.PaymentOrder;
import roomescape.payment.order.PaymentOrderRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentOrder order;

    @BeforeEach
    void setUp() {
        order = PaymentOrder.pending("order-1", "브라운", LocalDate.of(2099, 12, 31), 1L, 1L, 10000L);
    }

    @Test
    void 저장금액과_다른_amount면_확인전에_차단되고_게이트웨이는_호출되지_않는다() {
        given(orderRepository.getByOrderId("order-1")).willReturn(order);

        assertThatThrownBy(() -> paymentService.confirm("test_pk_1", "order-1", 9000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentGateway, never()).confirm(any());
    }

    @Test
    void 금액이_일치하면_게이트웨이를_호출한다() {
        given(orderRepository.getByOrderId("order-1")).willReturn(order);
        given(paymentGateway.confirm(any()))
                .willReturn(new PaymentResult("test_pk_1", "order-1", PaymentStatus.DONE, 10000L));

        PaymentResult result = paymentService.confirm("test_pk_1", "order-1", 10000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        verify(paymentGateway).confirm(new PaymentConfirmation("test_pk_1", "order-1", 10000L));
    }
}
