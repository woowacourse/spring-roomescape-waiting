package roomescape.presentation.api.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.reservation.controller.dto.request.CreateReservationUserRequest;

class CreateReservationUserRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    public static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @DisplayName("예약 생성시 값이 유효하지 않다면 예외를 발생시킨다")
    @ParameterizedTest(name = "[{index}] name={0}, date={1}, timeId={2}, themeId={3}")
    @CsvSource({
            // 유효하지 않은 date
            "null, 1, 1",

            // 유효하지 않은 timeId
            "2025-05-10, null, 1",

            // 유효하지 않은 themeId
            "2025-05-10, 1, null",

            // 복수의 필드가 유효하지 않음
            "null, null, null",
            "null, null, null",
            "null, 1, null",
    })
    void InvalidCreateFutureReservationReservationRequest(String dateStr, String timeIdStr, String themeIdStr) {
        LocalDate date = "null".equals(dateStr) ? null : LocalDate.parse(dateStr);
        Long timeId = "null".equals(timeIdStr) ? null : Long.valueOf(timeIdStr);
        Long themeId = "null".equals(themeIdStr) ? null : Long.valueOf(themeIdStr);

        CreateReservationUserRequest request = new CreateReservationUserRequest(date, timeId, themeId);

        Set<ConstraintViolation<CreateReservationUserRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @DisplayName("예약 생성시 값이 유효하다면 예외를 발생시키지 않는다")
    @Test
    void validCreateFutureReservationReservationRequest() {
        //given
        CreateReservationUserRequest request = new CreateReservationUserRequest(
                LocalDate.now().plusDays(1),
                1L,
                1L
        );

        //when
        Set<ConstraintViolation<CreateReservationUserRequest>> violations = validator.validate(request);

        //then
        assertThat(violations).isEmpty();
    }
}
