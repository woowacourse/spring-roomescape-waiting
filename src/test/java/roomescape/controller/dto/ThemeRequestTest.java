package roomescape.controller.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeRequestTest {

    private static final String VALID_NAME = "무인도 탈출";
    private static final String VALID_DESCRIPTION = "갯벌이 많은 무인도를 탈출하는 흥미진진 대탈출!";
    private static final String VALID_THUMBNAIL = "https://picsum.photos/seed/roomescape1/800/600.jpg";

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("모든 값이 유효하면 위반이 없다")
    void 모든_값이_유효하면_위반이_없다() {
        ThemeRequest request = new ThemeRequest(VALID_NAME, VALID_DESCRIPTION, VALID_THUMBNAIL);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름이 null이면 위반이 발생한다")
    void 이름이_null이면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest(null, VALID_DESCRIPTION, VALID_THUMBNAIL);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 빈문자열이면 위반이 발생한다")
    void 이름이_빈문자열이면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest("", VALID_DESCRIPTION, VALID_THUMBNAIL);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 이름은 필수입니다");
    }

    @Test
    @DisplayName("이름이 공백만으로 이루어져 있으면 위반이 발생한다")
    void 이름이_공백만으로_이루어져_있으면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest("   ", VALID_DESCRIPTION, VALID_THUMBNAIL);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 이름은 필수입니다");
    }

    @Test
    @DisplayName("설명이 null이면 위반이 발생한다")
    void 설명이_null이면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest(VALID_NAME, null, VALID_THUMBNAIL);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 설명은 필수입니다");
    }

    @Test
    @DisplayName("설명이 빈문자열이면 위반이 발생한다")
    void 설명이_빈문자열이면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest(VALID_NAME, "", VALID_THUMBNAIL);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 설명은 필수입니다");
    }

    @Test
    @DisplayName("썸네일이 null이면 위반이 발생한다")
    void 썸네일이_null이면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest(VALID_NAME, VALID_DESCRIPTION, null);

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 썸네일은 필수입니다");
    }

    @Test
    @DisplayName("썸네일이 빈문자열이면 위반이 발생한다")
    void 썸네일이_빈문자열이면_위반이_발생한다() {
        ThemeRequest request = new ThemeRequest(VALID_NAME, VALID_DESCRIPTION, "");

        Set<ConstraintViolation<ThemeRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("테마 썸네일은 필수입니다");
    }
}
