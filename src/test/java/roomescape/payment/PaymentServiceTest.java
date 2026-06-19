package roomescape.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

class PaymentServiceTest {

    private PaymentGateway paymentGateway;
    private PaymentService paymentService;

    @BeforeEach
    void beforeEach() {
        paymentGateway = Mockito.mock(PaymentGateway.class);
        paymentService = new PaymentService(paymentGateway);
    }

    @Test
    void confirmDelegatesGatewayTest() {
        PaymentResult expected = new PaymentResult("payment_key", "order_test", "DONE", 50000L);

        when(paymentGateway.confirm(new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L)))
                .thenReturn(expected);

        PaymentConfirmationResult result = paymentService.confirm("payment_key", "order_test", "order_test", 50000L);

        assertThat(result.paymentResult()).isEqualTo(expected);
        assertThat(result.unknown()).isFalse();
        verify(paymentGateway, times(1)).confirm(new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L));
    }

    @Test
    void confirmRetriesRetryableGatewayErrorWithSameIdempotencyKeyTest() {
        PaymentResult expected = new PaymentResult("payment_key", "order_test", "DONE", 50000L);
        PaymentConfirmation confirmation = new PaymentConfirmation("payment_key", "order_test", "stored_key", 50000L);
        when(paymentGateway.confirm(confirmation))
                .thenThrow(new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE))
                .thenThrow(new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE))
                .thenReturn(expected);

        PaymentConfirmationResult result = paymentService.confirm("payment_key", "order_test", "stored_key", 50000L);

        assertThat(result.paymentResult()).isEqualTo(expected);
        assertThat(result.unknown()).isFalse();
        assertConfirmAttemptsUseSameIdempotencyKey("stored_key", 3);
    }

    @Test
    void confirmRetriesUnknownPaymentAndKeepsUnknownAfterRetryBudgetTest() {
        PaymentConfirmation confirmation = new PaymentConfirmation("payment_key", "order_test", "stored_key", 50000L);
        when(paymentGateway.confirm(confirmation))
                .thenThrow(new RoomEscapeException(DomainErrorCode.PAYMENT_UNKNOWN));

        PaymentConfirmationResult result = paymentService.confirm("payment_key", "order_test", "stored_key", 50000L);

        assertThat(result.unknown()).isTrue();
        assertThat(result.paymentResult()).isNull();
        assertConfirmAttemptsUseSameIdempotencyKey("stored_key", 3);
    }

    @Test
    void confirmRetryableGatewayErrorThrowsAfterRetryBudgetTest() {
        PaymentConfirmation confirmation = new PaymentConfirmation("payment_key", "order_test", "stored_key", 50000L);
        when(paymentGateway.confirm(confirmation))
                .thenThrow(new RoomEscapeException(DomainErrorCode.PAYMENT_RETRYABLE));

        assertThatThrownBy(() -> paymentService.confirm("payment_key", "order_test", "stored_key", 50000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_RETRYABLE));
        assertConfirmAttemptsUseSameIdempotencyKey("stored_key", 3);
    }

    @Test
    void confirmDoesNotRetryNonRetryablePaymentErrorTest() {
        PaymentConfirmation confirmation = new PaymentConfirmation("payment_key", "order_test", "stored_key", 50000L);
        when(paymentGateway.confirm(confirmation))
                .thenThrow(new RoomEscapeException(DomainErrorCode.PAYMENT_REJECTED));

        assertThatThrownBy(() -> paymentService.confirm("payment_key", "order_test", "stored_key", 50000L))
                .isInstanceOf(RoomEscapeException.class)
                .satisfies(e -> assertThat(((RoomEscapeException) e).code())
                        .isEqualTo(DomainErrorCode.PAYMENT_REJECTED));
        verify(paymentGateway, times(1)).confirm(confirmation);
    }

    private void assertConfirmAttemptsUseSameIdempotencyKey(String idempotencyKey, int attempts) {
        ArgumentCaptor<PaymentConfirmation> captor = ArgumentCaptor.forClass(PaymentConfirmation.class);
        verify(paymentGateway, times(attempts)).confirm(captor.capture());
        List<String> idempotencyKeys = captor.getAllValues().stream()
                .map(PaymentConfirmation::idempotencyKey)
                .toList();

        assertThat(idempotencyKeys).containsOnly(idempotencyKey);
    }
}
