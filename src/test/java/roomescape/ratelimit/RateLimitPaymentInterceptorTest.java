package roomescape.ratelimit;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.controller.PaymentViewController;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.service.PaymentService;

@WebMvcTest(PaymentViewController.class)
@Import(RateLimitConfig.class)
@TestPropertySource(properties = {
        "toss.client-key=test_client_key",
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.1"
})
class RateLimitPaymentInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 승인 엔드포인트가 한도를 초과하면 429와 Retry-After 헤더를 반환하고 컨트롤러를 호출하지 않는다.")
    void rateLimitPayments() throws Exception {
        PaymentResult result = new PaymentResult("payment-key", "order-1", PaymentStatus.DONE, 50000L);
        given(paymentService.confirm(anyString(), anyString(), anyLong())).willReturn(result);

        performSuccessPayment()
                .andExpect(status().isOk());

        performSuccessPayment()
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "10"));

        verify(paymentService, times(1)).confirm(anyString(), anyString(), anyLong());
    }

    private org.springframework.test.web.servlet.ResultActions performSuccessPayment() throws Exception {
        return mockMvc.perform(get("/payments/success")
                .param("paymentKey", "payment-key")
                .param("orderId", "order-1")
                .param("amount", "50000"));
    }
}
