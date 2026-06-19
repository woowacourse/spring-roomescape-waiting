package roomescape.feature.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.feature.payment.dto.PaymentApproveRequest;

/**
 * 실제 토스 결제 승인 API(https://api.tosspayments.com)를 호출하는 통합 테스트.
 * 네트워크와 공개 테스트 시크릿 키에 의존하며, 실재하지 않는 결제 건으로 호출해
 * RestClient 설정(baseUrl·인증 헤더)·요청 직렬화·에러 변환이 실제 응답에도 동작하는지 검증한다.
 * (외부 의존 테스트이므로 CI 에서 제외하려면 useJUnitPlatform { excludeTags 'external' } 로 거를 수 있다.)
 */
@Tag("external")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentApproverIntegrationTest {

    private static final String NON_EXISTENT_ORDER_ID = "non_existent_order_id";
    private static final String NON_EXISTENT_PAYMENT_KEY = "non_existent_payment_key";
    private static final Long AMOUNT = 1_000L;

    @Autowired
    private PaymentApprover paymentApprover;

    @Test
    void 실재하지_않는_결제를_승인하면_토스가_에러를_응답하고_PaymentException으로_변환된다() {
        // given
        PaymentApproveRequest request =
                new PaymentApproveRequest(NON_EXISTENT_ORDER_ID, NON_EXISTENT_PAYMENT_KEY, AMOUNT);

        // when
        Throwable thrown = catchThrowable(() -> paymentApprover.approve(request));

        // then: 실제 토스 응답을 PaymentException 으로 변환한다. (code·message 는 토스가 정하므로 존재만 검증)
        assertThat(thrown).isInstanceOf(PaymentException.class);

        PaymentException exception = (PaymentException) thrown;
        assertThat(exception.getCode()).isNotBlank();
        assertThat(exception.getMessage()).isNotBlank();
        assertThat(exception.getFailureType()).isNotNull();
    }
}
