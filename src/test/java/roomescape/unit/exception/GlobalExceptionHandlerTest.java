package roomescape.unit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import roomescape.exception.GlobalExceptionHandler;
import roomescape.exception.PaymentAmountMismatchException;
import roomescape.exception.ProblemType;
import roomescape.payment.toss.TossPaymentException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    @Test
    void 토스_결제_오류는_502와_PAYMENT_FAILED로_매핑한다() {
        ProblemDetail problem = handler.handleTossPayment(new TossPaymentException("토스 승인 실패"), request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(problem.getType().toString()).isEqualTo(ProblemType.PAYMENT_FAILED.uri().toString());
        assertThat(problem.getDetail()).isEqualTo("결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    void 금액_불일치는_422와_PAYMENT_AMOUNT_MISMATCH로_매핑한다() {
        ProblemDetail problem = handler.handlePaymentAmountMismatch(
                new PaymentAmountMismatchException(50_000L, 999L),
                request
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(problem.getType().toString()).isEqualTo(ProblemType.PAYMENT_AMOUNT_MISMATCH.uri().toString());
    }
}
