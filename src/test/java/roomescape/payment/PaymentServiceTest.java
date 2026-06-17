package roomescape.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        PaymentResult result = paymentService.confirm("payment_key", "order_test", "order_test", 50000L);

        assertThat(result).isEqualTo(expected);
        verify(paymentGateway, times(1)).confirm(new PaymentConfirmation("payment_key", "order_test", "order_test", 50000L));
    }
}
