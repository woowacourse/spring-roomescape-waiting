package roomescape.global.error.handler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.MethodParameter;
import roomescape.global.error.dto.ErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.dto.ParameterErrorResponsesDto;
import roomescape.global.error.exception.GeneralNotFoundException;
import roomescape.domain.reservation.error.type.ReservationErrorType;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 필드_검증_오류는_parameterErrors로_함께_내려준다() throws Exception {
        // given
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "name", "예약자명은 필수입니다."));
        bindingResult.addError(new FieldError("request", "date", "예약 날짜가 현재보다 과거입니다."));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            createMethodParameter(), bindingResult);

        // when
        ResponseEntity<ParameterErrorResponsesDto> response = handler.handleValidationException(exception);

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("요청 값이 올바르지 않습니다.");
        assertThat(response.getBody().parameterErrors())
            .containsExactly(
                new ParameterErrorResponseDto("name", "예약자명은 필수입니다."),
                new ParameterErrorResponseDto("date", "예약 날짜가 현재보다 과거입니다.")
            );
    }

    @Test
    void 참조_자원_필드_오류는_parameterErrors로_함께_내려준다() {
        // given
        GeneralNotFoundException exception = new GeneralNotFoundException(
            ReservationErrorType.FIELD_RESOURCE_NOT_FOUND,
            List.of(
                new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."),
                new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")
            )
        );

        // when
        ResponseEntity<ParameterErrorResponsesDto> response = handler.handleReservationNotFoundException(exception);

        // then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("조회할 자원이 존재하지 않습니다.");
        assertThat(response.getBody().parameterErrors())
            .containsExactly(
                new ParameterErrorResponseDto("timeId", "존재 하지 않는 시간대입니다."),
                new ParameterErrorResponseDto("themeId", "존재 하지 않는 테마입니다.")
            );
    }

    @Test
    void 필수_Query_Parameter_누락은_message만_내려준다() {
        // given
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException(
            "name", "String");

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handleMissingServletRequestParameterException(exception);

        // then
        assertThat(response.getBody()).isEqualTo(new ErrorResponseDto("요청 형식이 올바르지 않습니다."));
    }

    @Test
    void 타입_불일치는_message만_내려준다() throws Exception {
        // given
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
            "abc", Long.class, "id", createMethodParameter(), new IllegalArgumentException());

        // when
        ResponseEntity<ErrorResponseDto> response = handler.handleMethodArgumentTypeMismatchException(exception);

        // then
        assertThat(response.getBody()).isEqualTo(new ErrorResponseDto("요청 형식이 올바르지 않습니다."));
    }

    private MethodParameter createMethodParameter() throws NoSuchMethodException {
        return new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleMethod", String.class),
            0
        );
    }

    @SuppressWarnings("unused")
    private void sampleMethod(String value) {
    }
}
