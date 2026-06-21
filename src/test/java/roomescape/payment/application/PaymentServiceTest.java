package roomescape.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.application.service.PaymentService;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderRepository;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.payment.domain.PaymentResult;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentOrderRepository paymentOrderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Test
    void callbackAmount가_저장된_금액과_다르면_승인을_호출하지_않는다() {
        PaymentService paymentService = paymentService();
        given(paymentOrderRepository.findByOrderId("ROOM_order123"))
                .willReturn(Optional.of(new PaymentOrder(
                        1L,
                        "ROOM_order123",
                        10_000L,
                        PaymentOrderStatus.PAYMENT_PENDING,
                        null,
                        "fixed-idempotency-key"
                )));

        PaymentConfirmation manipulated = new PaymentConfirmation(
                "payment-key", "ROOM_order123", 100L);

        assertThatThrownBy(() -> paymentService.confirm(manipulated))
                .isInstanceOf(PaymentException.class)
                .hasMessage("결제 금액이 주문 금액과 일치하지 않습니다.");

        verify(paymentGateway, never()).confirm(manipulated, "fixed-idempotency-key");
    }

    @Test
    void failUrl에_orderId가_없어도_예외가_발생하지_않는다() {
        PaymentService paymentService = paymentService();

        assertThatCode(() -> paymentService.fail("PAY_PROCESS_CANCELED", null))
                .doesNotThrowAnyException();

        verify(paymentOrderRepository, never()).markFailed(null);
    }

    @Test
    void 승인할_때_주문에_저장된_고정_멱등키를_사용한다() {
        PaymentService paymentService = paymentService();
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "payment-key",
                "ROOM_order123",
                10_000L
        );
        given(paymentOrderRepository.findByOrderId("ROOM_order123"))
                .willReturn(Optional.of(new PaymentOrder(
                        1L,
                        "ROOM_order123",
                        10_000L,
                        PaymentOrderStatus.CONFIRMATION_UNKNOWN,
                        "payment-key",
                        "fixed-idempotency-key"
                )));
        given(paymentGateway.confirm(confirmation, "fixed-idempotency-key"))
                .willReturn(new PaymentResult("payment-key", "ROOM_order123", 10_000L, "DONE"));

        paymentService.confirm(confirmation);

        verify(paymentGateway).confirm(confirmation, "fixed-idempotency-key");
        verify(paymentOrderRepository).confirm("ROOM_order123", "payment-key");
    }

    @Test
    void 이미_확정된_주문의_success_새로고침은_게이트웨이를_다시_호출하지_않는다() {
        PaymentService paymentService = paymentService();
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "payment-key",
                "ROOM_order123",
                10_000L
        );
        given(paymentOrderRepository.findByOrderId("ROOM_order123"))
                .willReturn(Optional.of(new PaymentOrder(
                        1L,
                        "ROOM_order123",
                        10_000L,
                        PaymentOrderStatus.CONFIRMED,
                        "payment-key",
                        "fixed-idempotency-key"
                )));

        PaymentResult result = paymentService.confirm(confirmation);

        assertThat(result.status()).isEqualTo("DONE");
        verify(paymentGateway, never()).confirm(confirmation, "fixed-idempotency-key");
    }

    private PaymentService paymentService() {
        return new PaymentService(
                paymentOrderRepository,
                paymentGateway,
                null,
                null,
                null,
                new PaymentProperties(
                        new PaymentProperties.Toss(
                                "https://api.tosspayments.com",
                                "test_ck_test",
                                "test_sk_test",
                                Duration.ofSeconds(2),
                                Duration.ofSeconds(3)
                        ),
                        10_000L
                )
        );
    }
}
