package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderStatus;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.NotFoundException;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.repository.FakeOrderRepository;
import roomescape.repository.FakePaymentRepository;

class PaymentServiceTest {

    private PaymentService paymentService;
    private FakeOrderRepository orderRepository;
    private FakePaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new FakeOrderRepository();
        paymentRepository = new FakePaymentRepository();
        paymentService = new PaymentService(orderRepository, new FakePaymentGateway(), paymentRepository);
    }

    @Test
    @DisplayName("주문 금액과 결제 요청 금액이 일치하면 결제를 승인하고 결과를 저장한다.")
    void 결제_승인() {
        orderRepository.save(new Order("order-1", 50000L, 1L));

        PaymentResult result = paymentService.confirm("payment-key", "order-1", 50000L);

        assertThat(result.paymentKey()).isEqualTo("payment-key");
        assertThat(result.orderId()).isEqualTo("order-1");
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
        assertThat(paymentRepository.findByOrderId("order-1")).isPresent();
    }

    @Test
    @DisplayName("주문을 찾을 수 없으면 예외가 발생한다.")
    void 주문_없음_예외_발생() {
        assertThatThrownBy(() -> paymentService.confirm("payment-key", "not-exists", 50000L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 주문 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("주문 금액과 결제 요청 금액이 다르면 예외가 발생한다.")
    void 결제_금액_불일치_예외_발생() {
        orderRepository.save(new Order("order-1", 50000L, 1L));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 10000L))
                .isInstanceOf(PaymentAmountMismatchException.class)
                .hasMessageContaining("결제 금액과 저장된 금액이 일치하지 않습니다.");
    }

    private static class FakePaymentGateway implements PaymentGateway {

        @Override
        public PaymentResult confirm(PaymentConfirmation confirmation) {
            return new PaymentResult(
                    confirmation.paymentKey(),
                    confirmation.orderId(),
                    PaymentStatus.DONE,
                    confirmation.amount()
            );
        }
    }
}
