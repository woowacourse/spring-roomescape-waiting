package roomescape.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 들어오는 요청 Rate Limit 검증. capacity=1 로 낮춰, 한도 내 요청은 통과하고
 * 한도 초과 요청은 컨트롤러 호출 없이 429 + Retry-After 로 거부됨을 본다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "rate-limit.capacity=1",
        "rate-limit.refill-per-second=0.001" // 테스트 도중 보충이 일어나지 않을 만큼 느리게
})
class RateLimitInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 한도내_요청은_통과하고_한도초과_요청은_429와_RetryAfter헤더를_받는다() throws Exception {
        mockMvc.perform(get("/user/reservations").param("name", "brown"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/user/reservations").param("name", "brown"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
