package roomescape.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MemberReservationRequestTest {

    private Validator validator;

    private final Long validThemeId = 1L;
    private final String validDate = "2024-12-20";
    private final Long validTimeId = 1L;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @DisplayName("테마 아이디가 입력되지 않으면 예외가 발생한다.")
    @Test
    void throw_exception_when_null_theme_id_input() {
        MemberReservationRequest requestDto = new MemberReservationRequest(null, validDate, validTimeId);

        Set<ConstraintViolation<MemberReservationRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("테마 아이디는 반드시 입력되어야 합니다.");
    }

    @DisplayName("테마 아이디가 자연수가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void throw_exception_when_not_natural_theme_id_input(Long themeId) {
        MemberReservationRequest requestDto = new MemberReservationRequest(themeId, validDate, validTimeId);

        Set<ConstraintViolation<MemberReservationRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("테마 아이디는 자연수여야 합니다. " + themeId + "은 사용할 수 없습니다.");
    }

    @DisplayName("잘못된 날짜 형식이 입력되면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"23", "1-12-20", "2014-11", "2015-13-01", "2016-02-30", "2019-09-31", "2022-05-00"})
    void throw_exception_when_invalid_date_format_input(String date) {
        MemberReservationRequest requestDto = new MemberReservationRequest(validThemeId, date, validTimeId);

        Set<ConstraintViolation<MemberReservationRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("날짜 입력 형식이 올바르지 않습니다. ex) 1999-11-30");
    }

    @DisplayName("시간 아이디가 입력되지 않으면 예외가 발생한다.")
    @Test
    void throw_exception_when_null_time_id_input() {
        MemberReservationRequest requestDto = new MemberReservationRequest(validThemeId, validDate, null);

        Set<ConstraintViolation<MemberReservationRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("예약 시간 아이디는 반드시 입력되어야 합니다.");
    }

    @DisplayName("시간 아이디가 자연수가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void throw_exception_when_not_natural_time_id_input(Long timeId) {
        MemberReservationRequest requestDto = new MemberReservationRequest(validThemeId, validDate, timeId);

        Set<ConstraintViolation<MemberReservationRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("예약 시간 아이디는 자연수여야 합니다. " + timeId + "은 사용할 수 없습니다.");
    }

    @DisplayName("유효한 예약 입력 시 정상 생성된다.")
    @Test
    void create_success() {
        MemberReservationRequest requestDto = new MemberReservationRequest(validThemeId, validDate, validTimeId);

        Set<ConstraintViolation<MemberReservationRequest>> violations = validator.validate(requestDto);

        assertThat(violations.size()).isEqualTo(0);
    }
}
