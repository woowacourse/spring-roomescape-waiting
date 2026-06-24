package roomescape.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void 결제_승인_요청을_클라이언트에_위임한다() {
        PaymentConfirmRequest request = new PaymentConfirmRequest("paymentKey", "orderId", 1000L);
        PaymentConfirmResponse expected = new PaymentConfirmResponse("paymentKey", "orderId", 1000L, "DONE");
        given(paymentClient.confirm(request)).willReturn(expected);

        PaymentConfirmResponse response = paymentService.confirm(request);

        assertThat(response).isEqualTo(expected);
        verify(paymentClient).confirm(request);
    }
}
