package roomescape.infrastructure.ratelimit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

class RateLimitInterceptorTest {

    @Test
    @DisplayName("한도 내 요청은 200, 한도 초과 요청은 컨트롤러 호출 없이 429와 Retry-After 헤더를 받는다")
    void rejectsOverLimitWith429AndRetryAfter() throws Exception {
        var clock = new AtomicLong(0);
        var limiter = new TokenBucketRateLimiter(1, 1.0, clock::get);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .addInterceptors(new RateLimitInterceptor(limiter))
                .build();

        mockMvc.perform(post("/payments/success"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/payments/success"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @RestController
    static class TestController {

        @PostMapping("/payments/success")
        public String confirm() {
            return "ok";
        }
    }
}