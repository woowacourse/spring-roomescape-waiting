package roomescape.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import roomescape.infrastructure.toss.TossPaymentException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("토스 결제 예외를 사용자 응답에 필요한 상태 코드와 메시지로 변환한다")
    void handleTossPaymentException() {
        TossPaymentException exception = new TossPaymentException.CardRejected("카드 결제가 거절되었습니다.");

        ResponseEntity<ErrorResponse> response = handler.handleTossPaymentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "REJECT_CARD_PAYMENT",
                "CardRejected",
                "카드 결제가 거절되었습니다."
        ));
    }
}
