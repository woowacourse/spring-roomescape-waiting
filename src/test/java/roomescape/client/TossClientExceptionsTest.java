package roomescape.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TossClientExceptionsTest {

    @Test
    void TossConnectionException은_원인을_보존하고_안내_메시지를_노출한다() {
        RuntimeException cause = new RuntimeException("connect timed out");

        TossConnectionException exception = new TossConnectionException(cause);

        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getMessage()).isEqualTo("결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    void TossConfirmResultUnknownException은_원인을_보존하고_안내_메시지를_노출한다() {
        RuntimeException cause = new RuntimeException("read timed out");

        TossConfirmResultUnknownException exception = new TossConfirmResultUnknownException(cause);

        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getMessage()).isEqualTo("결제 승인 결과를 확인하지 못했습니다. 결제가 완료되었을 수 있으니 예약 내역을 확인해주세요.");
    }

    @Test
    void TossRateLimitExceededException은_안내_메시지를_노출한다() {
        TossRateLimitExceededException exception = new TossRateLimitExceededException();

        assertThat(exception.getMessage()).isEqualTo("결제 승인 요청이 많아 토스 서버가 일시적으로 거부했습니다. 잠시 후 다시 시도해주세요.");
    }

    @Test
    void TossOutboundRateLimitException은_안내_메시지를_노출한다() {
        TossOutboundRateLimitException exception = new TossOutboundRateLimitException();

        assertThat(exception.getMessage()).isEqualTo("결제 승인 요청이 많아 잠시 후 다시 시도해주세요.");
    }
}
