package roomescape.support.exception;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import org.springframework.web.HttpRequestMethodNotSupportedException;

class GlobalExceptionHandlerTest {

    @Test
    void 지원하지_않는_HTTP_메서드_예외를_405_에러_응답으로_변환한다() {
        // given
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpRequestMethodNotSupportedException(
            exception);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode().value()).isEqualTo(405);
            softly.assertThat(response.getBody().code()).isEqualTo("METHOD_NOT_ALLOWED");
            softly.assertThat(response.getBody().message()).isEqualTo("해당 엔드포인트에서 지원하지 않는 HTTP 메서드입니다.");
        });
    }

    @Test
    void RoomescapeException을_에러_응답으로_변환한다() {
        // given
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        RoomescapeException exception = new RoomescapeException(ReservationErrorCode.INVALID_RESERVATION_NAME);

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRoomescapeException(exception);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode().value()).isEqualTo(400);
            softly.assertThat(response.getBody().code()).isEqualTo("INVALID_RESERVATION_NAME");
            softly.assertThat(response.getBody().message()).isEqualTo("예약자 성명 데이터가 유효하지 않습니다.");
        });
    }

    @Test
    void 예상하지_못한_예외를_500_에러_응답으로_변환한다() {
        // given
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        Exception exception = new IllegalStateException();

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode().value()).isEqualTo(500);
            softly.assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
            softly.assertThat(response.getBody().message()).isEqualTo("처리되지 않은 서버 내부 오류가 발생했습니다.");
        });
    }
}
