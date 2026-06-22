package roomescape.ratelimit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.payment.application.dto.PaymentOrderResult;
import roomescape.payment.application.service.PaymentService;
import roomescape.payment.presentation.controller.PaymentController;

@WebMvcTest(PaymentController.class)
@Import({RateLimitConfig.class, RateLimitWebConfig.class})
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-sec=0.001",
        "outbound-rate-limit.capacity=1",
        "outbound-rate-limit.refill-per-sec=1"
})
class RateLimitWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void 한도를_초과한_요청은_컨트롤러를_호출하지_않고_429로_거부한다() throws Exception {
        given(paymentService.createOrder(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .willReturn(new PaymentOrderResult("order-id", 10_000L, "예약", "client-key"));
        String body = """
                {
                  "name": "카야",
                  "date": "2028-05-06",
                  "themeId": 1,
                  "timeId": 1
                }
                """;

        mockMvc.perform(post("/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/payments/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "1000"));

        verify(paymentService, times(1))
                .createOrder(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
