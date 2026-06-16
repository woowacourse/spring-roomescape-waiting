package roomescape.payment.infra.toss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentInvalidRequestException;
import roomescape.payment.domain.exception.PaymentKeyConfigurationException;
import roomescape.payment.domain.exception.PaymentNotFoundException;
import roomescape.payment.domain.exception.PaymentRejectedException;
import roomescape.payment.domain.exception.PaymentRetryableException;

import static org.assertj.core.api.Assertions.assertThat;

class TossPaymentExceptionMapperTest {

    @Test
    @DisplayName("이미 승인된 결제 코드를 도메인 예외로 매핑한다.")
    void map_alreadyProcessedPayment() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                HttpStatus.BAD_REQUEST,
                new TossErrorResponse("ALREADY_PROCESSED_PAYMENT", "이미 처리되었습니다.")
        );

        assertThat(exception).isInstanceOf(PaymentAlreadyProcessedException.class);
    }

    @Test
    @DisplayName("카드 거절 코드를 사용자 안내 예외로 매핑한다.")
    void map_rejectCardPayment() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                HttpStatus.FORBIDDEN,
                new TossErrorResponse("REJECT_CARD_PAYMENT", "카드 결제가 거절되었습니다.")
        );

        assertThat(exception).isInstanceOf(PaymentRejectedException.class);
        assertThat(exception).hasMessage("카드 결제가 거절되었습니다.");
    }

    @Test
    @DisplayName("키 오류 코드를 설정 오류 예외로 매핑한다.")
    void map_invalidApiKey() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                HttpStatus.UNAUTHORIZED,
                new TossErrorResponse("INVALID_API_KEY", "잘못된 키입니다.")
        );

        assertThat(exception).isInstanceOf(PaymentKeyConfigurationException.class);
    }

    @Test
    @DisplayName("재시도 대상 코드를 재시도 가능 예외로 매핑한다.")
    void map_retryableInternalProcessingFailure() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                HttpStatus.INTERNAL_SERVER_ERROR,
                new TossErrorResponse("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", "일시 오류입니다.")
        );

        assertThat(exception).isInstanceOf(PaymentRetryableException.class);
    }

    @Test
    @DisplayName("잘못된 요청과 결제 없음 코드를 각각 매핑한다.")
    void map_invalidAndNotFoundCodes() {
        RuntimeException invalidRequest = TossPaymentExceptionMapper.map(
                HttpStatus.BAD_REQUEST,
                new TossErrorResponse("NOT_FOUND_PAYMENT_SESSION", "세션이 없습니다.")
        );
        RuntimeException notFound = TossPaymentExceptionMapper.map(
                HttpStatus.NOT_FOUND,
                new TossErrorResponse("NOT_FOUND_PAYMENT", "결제가 없습니다.")
        );

        assertThat(invalidRequest).isInstanceOf(PaymentInvalidRequestException.class);
        assertThat(notFound).isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("알 수 없는 코드는 기본 게이트웨이 예외로 매핑한다.")
    void map_unknownCode() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                HttpStatus.BAD_GATEWAY,
                new TossErrorResponse("NEW_UNKNOWN_CODE", "새 오류입니다.")
        );

        assertThat(exception).isInstanceOf(PaymentGatewayException.class);
    }
}
