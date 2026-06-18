package roomescape.global.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

class RateLimitInterceptorTest {

    private RateLimitInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        int capacity = 1;
        double refillPerSecond = 1.0;

        NanoClock mockClock = mock(NanoClock.class);
        when(mockClock.currentNanoseconds()).thenReturn(0L); // 초기 시간 0으로 고정
        RateLimitBuckets buckets = new RateLimitBuckets(
                capacity,
                refillPerSecond,
                mockClock
        );

        interceptor = new RateLimitInterceptor(buckets);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    class 애노테이션_탐색_및_통과_여부를_결정한다 {

        @Test
        void RateLimit_애노테이션이_없다면_true를_반환한다() throws NoSuchMethodException {
            // given
            HandlerMethod handlerWithoutAnnotation = new HandlerMethod(new DummyController(), "noLimit");

            // when
            boolean isAllowed = interceptor.preHandle(request, response, handlerWithoutAnnotation);

            // then
            assertThat(isAllowed).isTrue();
        }

        @Test
        void 메서드에_RateLimit이_있고_용량이_충분하다면_true를_반환한다() throws NoSuchMethodException {
            // given
            HandlerMethod handlerWithMethodAnnotation = new HandlerMethod(new DummyController(), "methodLevelLimit");

            // when
            boolean isAllowed = interceptor.preHandle(request, response, handlerWithMethodAnnotation);

            // then
            assertThat(isAllowed).isTrue();
        }

        @Test
        void 클래스에만_RateLimit이_있어도_이를_감지하고_true를_반환한다() throws NoSuchMethodException {
            // given
            HandlerMethod handlerWithClassAnnotation = new HandlerMethod(new DummyClassLevelController(), "classLevelLimit");

            // when
            boolean isAllowed = interceptor.preHandle(request, response, handlerWithClassAnnotation);

            // then
            assertThat(isAllowed).isTrue();
        }
    }

    @Nested
    class 한도_초과_시_응답을_제어한다 {

        @Test
        void 허용량을_초과하면_false를_반환하고_429_상태코드와_Retry_After_헤더를_세팅한다() throws NoSuchMethodException {
            // given
            HandlerMethod handlerMethod = new HandlerMethod(new DummyController(), "methodLevelLimit");

            interceptor.preHandle(request, response, handlerMethod); // 1회 호출 (허용량 1 소진)

            // when (2회차 호출 시도)
            boolean isAllowed = interceptor.preHandle(request, response, handlerMethod);

            // then
            assertThat(isAllowed).isFalse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
            assertThat(response.getHeader(HttpHeaders.RETRY_AFTER)).isEqualTo("1"); // 1초 대기 권장
        }
    }

    // --- 테스트를 위한 더미(Dummy) 컨트롤러 클래스들 ---

    static class DummyController {
        @RateLimit(key = "method-limit")
        public void methodLevelLimit() {
        }

        public void noLimit() {
        }
    }

    @RateLimit(key = "class-limit")
    static class DummyClassLevelController {
        public void classLevelLimit() {
        }
    }
}
