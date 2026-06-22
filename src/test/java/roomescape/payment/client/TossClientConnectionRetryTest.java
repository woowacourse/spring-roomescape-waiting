package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.client.TossPaymentGateway;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionFailedException;

/**
 * 연결 단계 실패(ResourceAccessException)는 요청이 토스에 도달조차 못 했으니 재시도가 안전하다. 이 테스트는 Spring 빈(@Retryable 이
 * 적용된 프록시)을 통해 실제로 재시도가 일어나고, 그래도 끝내 연결이 안 되면 PaymentConnectionFailedException(확정 실패)으로 떨어지는지
 * 검증한다.
 *
 * <p>{@code TossClientTimeoutTest}의 같은 시나리오 테스트는 {@code new TossPaymentGateway(...)}로 직접 생성한 인스턴스를 써
 * Spring AOP 프록시(=재시도)를 타지 않는다. 재시도까지 검증하려면 컨테이너가 만든 빈이어야 한다.
 */
@SpringBootTest
class TossClientConnectionRetryTest {

    // 응답 없는(SYN 무응답) IP → connect 가 매달려 connect timeout 을 유발한다.
    private static final String BLACKHOLE_URL = "http://10.255.255.1:81";

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> BLACKHOLE_URL);
        registry.add("toss.secret-key", () -> "test_gsk_dummy");
        registry.add("toss.connect-timeout-ms", () -> "500");
        registry.add("toss.read-timeout-ms", () -> "500");
    }

    private PaymentConfirmation confirmation() {
        return new PaymentConfirmation("test_pk_1", "order-1", 10000L);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = ThreadMode.SEPARATE_THREAD)
    void 연결실패가_반복되면_재시도를_모두_소진한_뒤_확정실패로_변환된다() {
        var start = System.nanoTime();
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation()))
                .isInstanceOf(PaymentConnectionFailedException.class)
                .hasRootCauseInstanceOf(SocketTimeoutException.class);
        var elapsedMs = (System.nanoTime() - start) / 1_000_000;

        // connect timeout(500ms) x 3회 시도 + 백오프(200ms) x 2회 = 최소 약 1,900ms.
        // 재시도 없이 한 번만 시도했다면 500ms 안팎에서 끝났을 것이므로, 하한선으로 "재시도가 실제로 일어났음"을 확인한다.
        assertThat(elapsedMs).isGreaterThan(1300L);
    }
}
