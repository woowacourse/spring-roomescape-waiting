package roomescape.adapter.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.RoomEscapeException;

/**
 * 실제 Toss 샌드박스 API를 호출하는 통합 스모크. 외부 호출이라 RUN_REAL_API=true 일 때만 실행.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_REAL_API", matches = "true")
class TossPaymentRealApiTest {

    // 본인 샌드박스 '결제창 시크릿 키'(test_sk_...). 클라이언트 키(test_ck_)와 같은 상점이어야 함.
    private static final String SECRET_KEY = "test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R";

    // 성공 케이스용: 결제창 인증 후 successUrl로 받은 실제 값을 채운다.
    private static final String PAYMENT_KEY = "";
    private static final String ORDER_ID = "";
    private static final long AMOUNT = 0L;

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> "https://api.tosspayments.com");
        registry.add("toss.secret-key", () -> SECRET_KEY);
        registry.add("toss.client-key", () -> "test_ck_dummy"); // confirm엔 안 쓰임
    }

    @Test
    void 존재하지_않는_결제건을_승인하면_토스가_에러를_내려주고_도메인_예외로_변환된다() {
        var confirmation = new PaymentConfirmation(
                "tgen_does_not_exist_payment_key", "real-api-smoke-order", 1000L);

        // 구체 코드는 상황 따라 달라질 수 있어, '우리 도메인(RoomEscape) 예외로 번역된다'만 검증
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    @Disabled("결제창 인증으로 얻은 PAYMENT_KEY/ORDER_ID/AMOUNT를 채우고 @Disabled를 지우면 실행")
    void 샌드박스에서_인증한_결제건을_승인하면_DONE_결과를_반환한다() {
        var result = tossPaymentGateway.confirm(
                new PaymentConfirmation(PAYMENT_KEY, ORDER_ID, AMOUNT));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(AMOUNT);
    }
}
