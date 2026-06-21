package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import roomescape.domain.Order;
import roomescape.domain.OrderStatus;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.exception.GatewayTimeoutException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentGateway paymentGateway;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.createWithId(1L, "order-001", "idem-key-001", 50000L, null, 1L, OrderStatus.PENDING);
    }

    @Nested
    class 결제_승인 {

        @Test
        void 성공() {
            PaymentResult paymentResult = new PaymentResult("pay-key-001", "order-001", 50000L, "DONE");
            given(orderRepository.findByOrderId("order-001")).willReturn(Optional.of(order));
            given(paymentGateway.confirm(any(PaymentConfirmation.class))).willReturn(paymentResult);

            PaymentResult result = paymentService.confirm("pay-key-001", "order-001", 50000L);

            assertThat(result.paymentKey()).isEqualTo("pay-key-001");
            assertThat(result.orderId()).isEqualTo("order-001");
            assertThat(result.approvedAmount()).isEqualTo(50000L);
            verify(orderRepository).confirm(1L, "pay-key-001");
        }

        @Test
        void 주문이_없으면_예외발생() {
            given(orderRepository.findByOrderId("order-001")).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.confirm("pay-key-001", "order-001", 50000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);
            verify(paymentGateway, never()).confirm(any());
        }

        @Test
        void 금액_불일치시_예외발생() {
            given(orderRepository.findByOrderId("order-001")).willReturn(Optional.of(order));

            assertThatThrownBy(() -> paymentService.confirm("pay-key-001", "order-001", 99999L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            verify(paymentGateway, never()).confirm(any());
        }

        @Test
        void confirm_호출시_멱등키가_헤더로_전달됨() {
            PaymentResult paymentResult = new PaymentResult("pay-key-001", "order-001", 50000L, "DONE");
            given(orderRepository.findByOrderId("order-001")).willReturn(Optional.of(order));
            given(paymentGateway.confirm(any(PaymentConfirmation.class))).willReturn(paymentResult);

            paymentService.confirm("pay-key-001", "order-001", 50000L);

            verify(paymentGateway).confirm(new PaymentConfirmation("pay-key-001", "order-001", "idem-key-001", 50000L));
        }

        @Test
        void 타임아웃_발생시_UNCERTAIN_처리() {
            given(orderRepository.findByOrderId("order-001")).willReturn(Optional.of(order));
            given(paymentGateway.confirm(any(PaymentConfirmation.class)))
                    .willThrow(new ResourceAccessException("read timeout"));

            assertThatThrownBy(() -> paymentService.confirm("pay-key-001", "order-001", 50000L))
                    .isInstanceOf(GatewayTimeoutException.class);
            verify(orderRepository).markUncertain(1L);
            verify(orderRepository, never()).confirm(any(), any());
        }
    }
}
