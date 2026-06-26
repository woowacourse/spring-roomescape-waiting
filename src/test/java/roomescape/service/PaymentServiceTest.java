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
import roomescape.exception.PaymentConfirmationUnknownException;
import roomescape.exception.PaymentConnectionException;
import roomescape.infra.toss.TossPaymentException;
import roomescape.repository.FakeOrderRepository;
import roomescape.repository.FakePaymentRepository;

class PaymentServiceTest {

    private PaymentService paymentService;
    private FakeOrderRepository orderRepository;
    private FakePaymentRepository paymentRepository;
    private FakePaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        orderRepository = new FakeOrderRepository();
        paymentRepository = new FakePaymentRepository();
        paymentGateway = new FakePaymentGateway();
        paymentService = new PaymentService(orderRepository, paymentGateway, paymentRepository);
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

    @Test
    @DisplayName("주문 금액과 결제 요청 금액이 다르면 승인 요청을 보내지 않는다.")
    void 결제_금액_불일치_승인_요청_차단() {
        orderRepository.save(new Order("order-1", 50000L, 1L));

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 10000L))
                .isInstanceOf(PaymentAmountMismatchException.class);

        assertThat(paymentGateway.isConfirmed()).isFalse();
        assertThat(paymentRepository.findByOrderId("order-1")).isEmpty();
        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("토스가 명확히 거절하면 주문 상태를 실패로 변경한다.")
    void 토스_거절_주문_실패() {
        orderRepository.save(new Order("order-1", 50000L, 1L));
        paymentGateway.reject();

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50000L))
                .isInstanceOf(TossPaymentException.class);

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().getStatus())
                .isEqualTo(OrderStatus.FAILED);
        assertThat(paymentRepository.findByOrderId("order-1")).isEmpty();
    }

    @Test
    @DisplayName("결제 승인 여부를 알 수 없으면 주문 상태를 확인 필요로 변경한다.")
    void 결제_결과_불명확_주문_확인_필요() {
        orderRepository.save(new Order("order-1", 50000L, 1L));
        paymentGateway.unknown();

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50000L))
                .isInstanceOf(PaymentConfirmationUnknownException.class);

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().getStatus())
                .isEqualTo(OrderStatus.REQUIRES_CONFIRMATION);
        assertThat(paymentRepository.findByOrderId("order-1")).isEmpty();
    }

    @Test
    @DisplayName("토스 연결 실패로 승인 요청을 완료하지 못하면 주문 상태를 대기로 유지한다.")
    void 토스_연결_실패_주문_대기_유지() {
        orderRepository.save(new Order("order-1", 50000L, 1L));
        paymentGateway.connectionFailed();

        assertThatThrownBy(() -> paymentService.confirm("payment-key", "order-1", 50000L))
                .isInstanceOf(PaymentConnectionException.class);

        assertThat(orderRepository.findByOrderId("order-1").orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING);
        assertThat(paymentRepository.findByOrderId("order-1")).isEmpty();
    }

    private static class FakePaymentGateway implements PaymentGateway {

        private boolean confirmed;
        private RuntimeException exception;

        @Override
        public PaymentResult confirm(PaymentConfirmation confirmation) {
            confirmed = true;
            if (exception != null) {
                throw exception;
            }
            return new PaymentResult(
                    confirmation.paymentKey(),
                    confirmation.orderId(),
                    PaymentStatus.DONE,
                    confirmation.amount()
            );
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public void reject() {
            exception = new TossPaymentException.CardRejected("카드 결제가 거절되었습니다.");
        }

        public void unknown() {
            exception = new PaymentConfirmationUnknownException();
        }

        public void connectionFailed() {
            exception = new PaymentConnectionException(new RuntimeException());
        }
    }
}
