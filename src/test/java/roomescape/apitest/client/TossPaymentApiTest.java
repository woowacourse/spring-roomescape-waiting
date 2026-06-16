package roomescape.apitest.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.domain.order.PaymentStatus;
import roomescape.infrastructure.payment.PaymentConfirmation;
import roomescape.infrastructure.payment.toss.TossPaymentException;
import roomescape.infrastructure.payment.toss.TossPaymentGateway;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_REAL_API", matches = "true")
public class TossPaymentApiTest {

    // 토스가 공개한 문서 테스트 시크릿 키(개인 비밀키 아님 → 하드코딩해도 안전).
    private static final String SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

    // 성공 케이스용: 샌드박스에서 결제 인증 후 successUrl 로 받은 값을 직접 넣으세요.
    private static final String PAYMENT_KEY = "tgen_20260616133123TvlH9";
    private static final String ORDER_ID = "MC42Mjg0OTg5NzYzOTMw";
    private static final long AMOUNT = 50_000L;

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> "https://api.tosspayments.com");
        registry.add("toss.secret-key", () -> SECRET_KEY);
    }

    @Test
    void 존재하지_않는_결제건을_승인하면_토스가_에러를_내려주고_도메인_예외로_변환된다() {
        // 유효한 결제 인증을 거치지 않은 임의의 paymentKey/orderId -> 토스가 4xx 에러를 응답한다.
        var confirmation = new PaymentConfirmation(
                "tgen_does_not_exist_payment_key",
                "real-api-test-order-id",
                1000L);

        // 토스가 주는 구체 코드는 상황에 따라 다를 수 있으므로(NOT_FOUND_PAYMENT / INVALID_REQUEST 등),
        // 어댑터가 'TossPaymentException 으로 변환한다'는 사실만 검증한다.
        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(TossPaymentException.class);
    }

    @Test
    @Disabled("PAYMENT_KEY / ORDER_ID / AMOUNT 상수에 샌드박스 결제로 얻은 값을 직접 넣고 이 @Disabled 를 지우면 실행됩니다.")
    void 샌드박스에서_인증한_결제건을_승인하면_DONE_결과를_반환한다() {
        // 같은 paymentKey 는 한 번만 승인된다(두 번째부터 ALREADY_PROCESSED_PAYMENT).
        var result = tossPaymentGateway.confirm(
                new PaymentConfirmation(PAYMENT_KEY, ORDER_ID, AMOUNT));

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(result.approvedAmount()).isEqualTo(AMOUNT);
    }

}
