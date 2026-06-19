package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class RateLimitWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void user_엔드포인트는_한도초과시_429지만_토스_콜백은_제한되지_않는다() throws Exception {
        mockMvc.perform(get("/user/reservations").param("name", "브라운"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/reservations").param("name", "브라운"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));

        mockMvc.perform(get("/payments/fail").param("orderId", "no-such"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(429));
    }
}
