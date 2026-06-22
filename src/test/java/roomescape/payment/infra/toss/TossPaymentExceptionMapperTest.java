package roomescape.payment.infra.toss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentConfirmationPendingException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentInvalidRequestException;
import roomescape.payment.domain.exception.PaymentKeyConfigurationException;
import roomescape.payment.domain.exception.PaymentNotFoundException;
import roomescape.payment.domain.exception.PaymentRejectedException;
import roomescape.payment.domain.exception.PaymentRetryableException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

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

    @Test
    @DisplayName("ResourceAccessException의 connect timeout은 연결 단계 실패로 매핑한다.")
    void map_resourceAccessConnectTimeout() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                new ResourceAccessException("I/O error", new SocketTimeoutException("Connect timed out"))
        );

        assertThat(exception)
                .isInstanceOf(PaymentConfirmationPendingException.class)
                .hasMessage("결제 승인 서버에 연결하지 못했습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.");
    }

    @Test
    @DisplayName("ResourceAccessException의 read timeout은 연결 단계 분기에서 임의로 응답 읽기 실패로 바꾸지 않는다.")
    void map_resourceAccessReadTimeout() {
        ResourceAccessException resourceAccessException = new ResourceAccessException(
                "I/O error",
                new SocketTimeoutException("Read timed out")
        );

        RuntimeException exception = TossPaymentExceptionMapper.map(resourceAccessException);

        assertThat(exception).isSameAs(resourceAccessException);
    }

    @Test
    @DisplayName("RestClientException의 소켓 타임아웃은 응답 읽기 실패로 매핑한다.")
    void map_restClientSocketTimeout() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                new RestClientException("Read timed out", new SocketTimeoutException("Read timed out"))
        );

        assertThat(exception)
                .isInstanceOf(PaymentConfirmationPendingException.class)
                .hasMessage("결제 승인 요청에 응답이 없습니다. 승인 여부가 확인되지 않았습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.");
    }

    @Test
    @DisplayName("연결 실패는 결제 결과 확인 필요 예외로 매핑한다.")
    void map_connectException() {
        RuntimeException exception = TossPaymentExceptionMapper.map(
                new ResourceAccessException("I/O error", new ConnectException("Connection refused"))
        );

        assertThat(exception)
                .isInstanceOf(PaymentConfirmationPendingException.class)
                .hasMessage("결제 승인 서버에 연결하지 못했습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.");
    }

    @Test
    @DisplayName("네트워크 원인이 아닌 RestClient 예외는 그대로 유지한다.")
    void map_nonNetworkRestClientException() {
        RestClientException restClientException = new RestClientException("message conversion failed");

        RuntimeException exception = TossPaymentExceptionMapper.map(restClientException);

        assertThat(exception).isSameAs(restClientException);
    }
}
