package roomescape.global.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RateLimitInterceptorTest {

    @Test
    @DisplayName("한도 초과 요청은 429와 Retry-After로 거부되고 컨트롤러를 호출하지 않는다.")
    void preHandle_rejectsTooManyRequestsBeforeController() throws Exception {
        AtomicLong clock = new AtomicLong(0);
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1.0, clock::get);
        TestReservationController controller = new TestReservationController();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new RateLimitInterceptor(rateLimiter))
                .build();

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "1"));

        assertThat(controller.callCount()).isEqualTo(1);
    }

    @RestController
    @RequestMapping("/reservations")
    private static class TestReservationController {
        private final AtomicInteger callCount = new AtomicInteger();

        @GetMapping
        ResponseEntity<Void> list() {
            callCount.incrementAndGet();
            return ResponseEntity.ok().build();
        }

        int callCount() {
            return callCount.get();
        }
    }
}
