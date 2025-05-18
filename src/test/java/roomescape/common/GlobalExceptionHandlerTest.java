package roomescape.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import roomescape.common.exception.CustomException;
import roomescape.common.exception.InvalidDateException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @DisplayName("IllegalArgumentException이 발생하면 400 Bad Request와 함께 메시지를 반환한다.")
    @Test
    void handle_illegal_argument_exception() {
        ResponseEntity<String> response = globalExceptionHandler.handleException();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("NullPointerException이 발생하면 400 Bad Request와 함께 메시지를 반환한다.")
    @Test
    void handle_null_pointer_exception() {
        ResponseEntity<String> response = globalExceptionHandler.handleException();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("CustomException이 발생하면 400 Bad Request와 함께 [ERROR] 접두사가 붙은 메시지를 반환한다.")
    @Test
    void handle_custom_exception() {
        String errorMessage = "날짜를 입력해주세요";
        CustomException exception = new InvalidDateException(errorMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleCustomException(exception);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            softly.assertThat(response.getBody()).isEqualTo("[ERROR] " + errorMessage);
        });
    }
}
