package roomescape.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("DB 조회 결과가 없을 때 발생하는 예외를 핸들러에서 404 응답 코드를 반환한다")
    void handleNotFoundException() {
        // given
        NotFoundException notFoundException = new NotFoundException("조회 결과가 없습니다.");

        // when
        ResponseEntity<String> responseEntity = globalExceptionHandler.handleBusinessException(notFoundException);

        // then
        HttpStatusCode responseHttpStatusCode = responseEntity.getStatusCode();
        assertThat(responseHttpStatusCode).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("유효하지 않은 값을 사용할 때 발생하는 예외를 핸들러에서 400 응답 코드를 반환한다")
    void handleIllegalArgumentException() {
        // given
        BadRequestException badRequestException = new BadRequestException("유효하지 않은 값입니다.");

        // when
        ResponseEntity<String> responseEntity = globalExceptionHandler.handleBusinessException(badRequestException);

        // then
        HttpStatusCode responseHttpStatusCode = responseEntity.getStatusCode();
        assertThat(responseHttpStatusCode).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("인증과 인가 과정에서 발생하는 예외를 핸들러에서 401 응답 코드를 반환한다")
    void handleUnauthorizedException() {
        // given
        UnauthorizedException unauthorizedException = new UnauthorizedException("인증, 인가 중 에러가 발생했습니다.");

        // when
        ResponseEntity<String> responseEntity = globalExceptionHandler.handleBusinessException(
                unauthorizedException);

        // then
        HttpStatusCode responseHttpStatusCode = responseEntity.getStatusCode();
        assertThat(responseHttpStatusCode).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
