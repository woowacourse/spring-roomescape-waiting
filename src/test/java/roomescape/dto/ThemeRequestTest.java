package roomescape.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ThemeRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 이름이_비어있으면_검증에_실패한다() {
        ThemeRequest request = new ThemeRequest("", "방탈출 설명", "thumbnail.png");

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 설명이_비어있으면_검증에_실패한다() {
        ThemeRequest request = new ThemeRequest("방탈출 제목", "", "thumbnail.png");

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void 썸네일이_비어있으면_검증에_실패한다() {
        ThemeRequest request = new ThemeRequest("방탈출 제목", "방탈출 설명", "");

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

}
