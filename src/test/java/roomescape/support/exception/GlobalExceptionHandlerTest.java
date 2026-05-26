package roomescape.support.exception;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import roomescape.support.exception.errors.ReservationErrors;

class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("RoomescapeException을 에러 응답으로 변환한다.")
    void convertRoomescapeExceptionToErrorResponse() {
        // given
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        BadRequestException exception = new BadRequestException(ReservationErrors.INVALID_RESERVATION_NAME);

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequestException(exception);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode().value()).isEqualTo(400);
            softly.assertThat(response.getBody().code()).isEqualTo("INVALID_RESERVATION_NAME");
            softly.assertThat(response.getBody().message()).isEqualTo("이름은 비어 있을 수 없습니다.");
        });
    }

    @Test
    @DisplayName("예상하지 못한 예외를 500 에러 응답으로 변환한다.")
    void convertUnexpectedExceptionToInternalServerErrorResponse() {
        // given
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        Exception exception = new IllegalStateException();

        // when
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode().value()).isEqualTo(500);
            softly.assertThat(response.getBody().code()).isEqualTo("INTERNAL_SERVER_ERROR");
            softly.assertThat(response.getBody().message()).isEqualTo("서버 내부 오류가 발생했습니다.");
        });
    }
}
