package roomescape.ratelimit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.001"
})
class RateLimitInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 한도내_요청은_컨트롤러까지_도달하고_한도초과_요청은_429와_RetryAfter헤더를_받는다() throws Exception {
        mockMvc.perform(post("/payments/confirm")
                        .contentType(APPLICATION_JSON)
                        .content("{\"paymentKey\":\"pk\",\"orderId\":\"order-1\",\"amount\":1000}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/payments/confirm")
                        .contentType(APPLICATION_JSON)
                        .content("{\"paymentKey\":\"pk\",\"orderId\":\"order-1\",\"amount\":1000}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
