package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

class InboundRateLimitInterceptorTest {

    @Test
    void blocksRequestWhenBucketIsEmptyTest() throws Exception {
        AtomicLong now = new AtomicLong(0L);
        InboundRateLimitInterceptor interceptor = new InboundRateLimitInterceptor(new TokenBucket(1, 0.1D, now::get));
        MockHttpServletRequest firstRequest = request();
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        MockHttpServletRequest secondRequest = request();
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        boolean firstAllowed = interceptor.preHandle(firstRequest, firstResponse, new Object());
        boolean secondAllowed = interceptor.preHandle(secondRequest, secondResponse, new Object());

        assertThat(firstAllowed).isTrue();
        assertThat(secondAllowed).isFalse();
        assertThat(secondResponse.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(secondResponse.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("10");
        assertThat(secondResponse.getContentAsString()).isEmpty();
    }

    @Test
    void doesNotInvokeControllerWhenBucketIsEmptyTest() throws Exception {
        AtomicLong now = new AtomicLong(0L);
        AtomicInteger calls = new AtomicInteger();
        InboundRateLimitInterceptor interceptor = new InboundRateLimitInterceptor(new TokenBucket(1, 0.1D, now::get));
        var mockMvc = MockMvcBuilders.standaloneSetup(new CountingController(calls))
                .addInterceptors(interceptor)
                .build();

        mockMvc.perform(post("/payments/confirm"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/payments/confirm"))
                .andExpect(status().isTooManyRequests());

        assertThat(calls.get()).isEqualTo(1);
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/payments/confirm");
        return request;
    }

    @RestController
    private static class CountingController {

        private final AtomicInteger calls;

        private CountingController(AtomicInteger calls) {
            this.calls = calls;
        }

        @PostMapping("/payments/confirm")
        ResponseEntity<Void> confirm() {
            calls.incrementAndGet();
            return ResponseEntity.ok().build();
        }
    }
}
