package roomescape.service.dto.theme;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ThemeRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @DisplayName("이름이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_name_input(String name) {
        ThemeRequest requestDto = new ThemeRequest(name, "하이", "hi.jpg");

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("이름은 반드시 입력되어야 합니다.");
    }

    @DisplayName("설명이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_description_input(String description) {
        ThemeRequest requestDto = new ThemeRequest("재즈의 신나는 프로그래밍", description, "hi.jpg");

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("설명은 반드시 입력되어야 합니다.");
    }

    @DisplayName("썸네일이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_thumbnail_input(String thumbnail) {
        ThemeRequest requestDto = new ThemeRequest("재즈의 신나는 프로그래밍", "하이", thumbnail);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("썸네일은 반드시 입력되어야 합니다.");
    }

}
