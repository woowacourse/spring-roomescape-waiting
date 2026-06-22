package roomescape.global.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 들어오는 Rate Limit 검증. capacity=1 로 외부화 값을 오버라이드해 결정적으로 본다.
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
    @DisplayName("한도 내 요청은 200, 한도 초과 요청은 컨트롤러 호출 없이 429 + Retry-After 를 받는다")
    void 한도초과_요청은_429와_RetryAfter() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
