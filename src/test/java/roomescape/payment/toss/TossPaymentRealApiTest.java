package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentResult;

/**
 * 실제 토스 샌드박스 API를 호출하는 통합 테스트. 외부 호출이라 환경변수 RUN_REAL_API=true 일 때만 실행된다.
 * 평소 빌드에선 클래스 전체가 스킵된다(네트워크·외부 의존 격리).
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RUN_REAL_API", matches = "true")
class TossPaymentRealApiTest {

    // 토스가 공개한 "문서 테스트" 시크릿 키 — 개인 비밀키가 아니라 공개 키라 하드코딩해도 안전하다.
    private static final String DOCS_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";

    // 성공 케이스용: 샌드박스에서 위젯 결제 인증 후 successUrl로 받은 값을 채우고 아래 @Disabled를 지우면 실행된다.
    private static final String PAYMENT_KEY = "";
    private static final String ORDER_ID = "";
    private static final long AMOUNT = 0L;

    @Autowired
    private TossPaymentGateway tossPaymentGateway;

    @DynamicPropertySource
    static void tossProperties(DynamicPropertyRegistry registry) {
        registry.add("toss.base-url", () -> "https://api.tosspayments.com");
        registry.add("toss.secret-key", () -> DOCS_SECRET_KEY);
    }

    @Test
    void 존재하지_않는_결제건을_승인하면_토스_에러를_도메인_예외로_변환한다() {
        // 유효한 결제 인증을 거치지 않은 임의의 paymentKey/orderId → 토스가 4xx 에러를 응답한다.
        // 구체 코드는 상황에 따라 다를 수 있어(NOT_FOUND_PAYMENT/INVALID_REQUEST 등) "TossPaymentException으로 변환"만 검증.
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "tgen_does_not_exist_payment_key", "real-api-test-order", 1000L);

        assertThatThrownBy(() -> tossPaymentGateway.confirm(confirmation))
                .isInstanceOf(TossPaymentException.class);
    }

    @Test
    @Disabled("PAYMENT_KEY/ORDER_ID/AMOUNT에 샌드박스 결제로 얻은 값을 채우고 이 @Disabled를 지우면 실행됩니다.")
    void 샌드박스에서_인증한_결제건을_승인하면_DONE을_반환한다() {
        // 같은 paymentKey는 한 번만 승인된다(두 번째부터 ALREADY_PROCESSED_PAYMENT).
        PaymentResult result = tossPaymentGateway.confirm(
                new PaymentConfirmation(PAYMENT_KEY, ORDER_ID, AMOUNT));

        assertThat(result.status()).isEqualTo("DONE");
        assertThat(result.totalAmount()).isEqualTo(AMOUNT);
    }
}
